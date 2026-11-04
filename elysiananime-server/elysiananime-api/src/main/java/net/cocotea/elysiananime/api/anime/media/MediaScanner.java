package net.cocotea.elysiananime.api.anime.media;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.playback.ProgressBackfill;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.service.RedisService;
import net.cocotea.elysiananime.properties.FileProp;
import org.noear.redisx.RedisClient;
import org.noear.redisx.plus.RedisLock;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Destroy;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Init;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * 媒体扫描编排：定时/启动/事件触发、Redis 锁、待扫队列、状态对象。
 * <p>
 * 单线程执行，只有全量扫描持锁；全量扫描进行中收到的单部触发进待扫队列，扫描结束后补跑。
 * {@code @Scheduled} 只有编译期常量，不能表达可配置间隔，所以这里自管
 * {@link ScheduledExecutorService} 并在每次调度前读取 {@code elysiananime.media.scan.interval}。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Component
public class MediaScanner {

    private static final Logger log = LoggerFactory.getLogger(MediaScanner.class);

    /**
     * 触发来源：扫描
     */
    public static final int SOURCE_SCAN = 1;

    /**
     * 触发来源：RSS 下载/重命名
     */
    public static final int SOURCE_RSS = 2;

    /**
     * 触发来源：手动上传
     */
    public static final int SOURCE_UPLOAD = 3;

    private static final String LOCK_KEY = "media:scan:lock";

    private static final int LOCK_TTL_SECONDS = 30 * 60;

    private static final long STARTUP_DELAY_SECONDS = 20;

    private static final long EVENT_DEBOUNCE_MS = 30_000;

    private static final String INTERVAL_PROP = "elysiananime.media.scan.interval";

    private static final long DEFAULT_INTERVAL_MS = 15 * 60_000L;

    private static final long MIN_INTERVAL_MS = 60_000L;

    @Inject
    private FileProp fileProp;

    @Inject
    private RedisService redisService;

    @Inject
    private RedisClient redisClient;

    @Inject
    private MediaScanExecutor mediaScanExecutor;

    @Inject
    private ProgressBackfill progressBackfill;

    @Inject
    private UpdateBackfill updateBackfill;

    @Db
    private LightDao lightDao;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            runnable -> new Thread(runnable, "media-scan-scheduler"));

    private final ExecutorService worker = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "media-scan-worker"));

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final Map<BigInteger, Integer> pendingOpus = new ConcurrentHashMap<>();

    private final AtomicReference<ScheduledFuture<?>> pendingEvent = new AtomicReference<>();

    private volatile MediaScanStatus status = new MediaScanStatus();

    @Init
    public void init() {
        scheduler.schedule(() -> scanAllAsync(SOURCE_SCAN), STARTUP_DELAY_SECONDS, TimeUnit.SECONDS);
        schedulePeriodic();
    }

    @Destroy
    public void destroy() {
        scheduler.shutdownNow();
        worker.shutdownNow();
    }

    /**
     * 触发全量扫描（异步），已有扫描运行时直接忽略
     */
    public void scanAllAsync(int source) {
        if (!running.compareAndSet(false, true)) {
            log.info("全量扫描进行中，忽略本次触发，source={}", source);
            return;
        }
        worker.execute(() -> scanAll(source));
    }

    /**
     * 触发单部扫描（异步）：30 秒内按 opusId 合并；全量扫描期间只排队，不抢锁
     */
    public void scanOpusAsync(BigInteger opusId, int source) {
        if (opusId == null) {
            return;
        }
        pendingOpus.put(opusId, source);
        if (running.get()) {
            return;
        }
        schedulePending();
    }

    /**
     * 扫描状态：运行中以当前实例为准，空闲时优先返回 Redis 里的上一次结果
     */
    public MediaScanStatus getStatus() {
        MediaScanStatus current = status;
        if (current.isRunning()) {
            return current;
        }
        try {
            String json = redisService.get(RedisKeyConst.MEDIA_SCAN_STATUS);
            if (StrUtil.isNotBlank(json)) {
                return JSON.parseObject(json, MediaScanStatus.class);
            }
        } catch (Exception e) {
            log.warn("读取扫描状态缓存失败：{}", e.getMessage());
        }
        return current;
    }

    private void scanAll(int source) {
        MediaScanResult result = new MediaScanResult();
        long begin = System.currentTimeMillis();
        String startedAt = DateUtil.now();
        result.setStartedAt(startedAt);
        status = new MediaScanStatus().setRunning(true).setStartedAt(startedAt);

        String holder = UUID.randomUUID().toString();
        RedisLock lock = redisClient.getLock(LOCK_KEY);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_TTL_SECONDS, holder);
            if (!locked) {
                result.setMessage("其他实例正在扫描，本次跳过");
                log.info("其他实例正在扫描，本次跳过 source={}", source);
                return;
            }
            scanRoots(source, result);
            // 索引建好之后再把旧进度搬进按集进度表
            progressBackfill.runIfNeeded();
            // 每部番的最新一集补进更新动态，老数据不至于空白
            updateBackfill.runIfNeeded();
        } catch (Throwable e) {
            result.setFailed(result.getFailed() + 1);
            result.setMessage(e.getMessage());
            log.error("全量扫描失败", e);
        } finally {
            if (locked) {
                lock.unLock(holder);
            }
            running.set(false);
            result.setFinishedAt(DateUtil.now());
            result.setDurationMs(System.currentTimeMillis() - begin);
            status.setRunning(false)
                    .setLastResult(result)
                    .setFailed(result.getFailed())
                    .setOrphans(result.getOrphans());
            persistStatus();
            schedulePending();
        }
    }

    private void scanRoots(int source, MediaScanResult result) {
        List<AniOpus> opusList = lightDao.findEntity(AniOpus.class,
                EntityQuery.create().where("is_deleted = 0"));
        Map<String, AniOpus> byMediaDir = new HashMap<>(opusList.size() * 2);
        Map<String, AniOpus> byName = new HashMap<>(opusList.size() * 2);
        for (AniOpus opus : opusList) {
            if (StrUtil.isNotBlank(opus.getMediaDir())) {
                byMediaDir.putIfAbsent(MediaPathUtils.normalize(opus.getMediaDir()), opus);
            }
            if (StrUtil.isNotBlank(opus.getNameCn())) {
                byName.putIfAbsent(opus.getNameCn(), opus);
            }
        }
        status.setTotal(opusList.size());

        String rawRoots = fileProp.getAnimationSavePath();
        if (StrUtil.isBlank(rawRoots)) {
            result.setMessage("未配置 elysiananime.file.animation-path");
            return;
        }
        Set<BigInteger> scanned = new HashSet<>();
        for (String rootText : rawRoots.split(",")) {
            if (StrUtil.isBlank(rootText)) {
                continue;
            }
            Path root = Paths.get(rootText.trim());
            if (!Files.isDirectory(root)) {
                result.setFailed(result.getFailed() + 1);
                log.warn("资源根目录不可用：{}", root);
                continue;
            }
            List<Path> opusDirs;
            try (Stream<Path> stream = Files.list(root)) {
                opusDirs = stream.filter(Files::isDirectory).sorted().toList();
            } catch (IOException e) {
                result.setFailed(result.getFailed() + 1);
                log.error("读取资源根目录失败：{}", root, e);
                continue;
            }
            for (Path dir : opusDirs) {
                String dirName = dir.getFileName().toString();
                if (dirName.startsWith(".")) {
                    continue;
                }
                AniOpus opus = byMediaDir.get(MediaPathUtils.normalize(dir.toString()));
                if (opus == null) {
                    opus = byName.get(dirName);
                }
                if (opus == null) {
                    result.setOrphans(result.getOrphans() + 1);
                    status.setOrphans(result.getOrphans());
                    continue;
                }
                if (!scanned.add(opus.getId())) {
                    result.setConflicts(result.getConflicts() + 1);
                    log.warn("作品《{}》出现在多个资源目录，以已记录的目录为准，跳过 {}", opus.getNameCn(), dir);
                    continue;
                }
                scanOne(opus, dir, source, result);
                status.setDone(status.getDone() + 1).setFailed(result.getFailed());
            }
        }
    }

    private void scanOne(AniOpus opus, Path dir, int source, MediaScanResult result) {
        try {
            MediaScanExecutor.Outcome outcome = mediaScanExecutor.scanOpus(opus, dir, source);
            result.setOpusTotal(result.getOpusTotal() + 1);
            result.setAdded(result.getAdded() + outcome.added);
            result.setUpdated(result.getUpdated() + outcome.updated);
            result.setRemoved(result.getRemoved() + outcome.removed);
            result.setRevived(result.getRevived() + outcome.revived);
            result.setRenamed(result.getRenamed() + outcome.renamed);
            result.setSkipped(result.getSkipped() + outcome.skipped);
            result.setUpdateRecords(result.getUpdateRecords() + outcome.updateRecords);
            if (!outcome.complete) {
                result.setFailed(result.getFailed() + 1);
            }
        } catch (Exception e) {
            result.setFailed(result.getFailed() + 1);
            log.error("扫描作品失败：《{}》({})", opus.getNameCn(), opus.getId(), e);
        }
    }

    private void schedulePeriodic() {
        if (scheduler.isShutdown()) {
            return;
        }
        long delay = scanIntervalMillis();
        try {
            scheduler.schedule(() -> {
                try {
                    scanAllAsync(SOURCE_SCAN);
                } finally {
                    schedulePeriodic();
                }
            }, delay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ignored) {
            // 关闭中
        }
    }

    private long scanIntervalMillis() {
        String raw = Solon.cfg().get(INTERVAL_PROP, "15m");
        if (StrUtil.isBlank(raw)) {
            return DEFAULT_INTERVAL_MS;
        }
        try {
            String text = raw.trim().toLowerCase(Locale.ROOT);
            long unit = 60_000L;
            if (text.endsWith("ms")) {
                unit = 1L;
                text = text.substring(0, text.length() - 2);
            } else if (text.endsWith("s")) {
                unit = 1_000L;
                text = text.substring(0, text.length() - 1);
            } else if (text.endsWith("m")) {
                text = text.substring(0, text.length() - 1);
            } else if (text.endsWith("h")) {
                unit = 3_600_000L;
                text = text.substring(0, text.length() - 1);
            } else if (text.endsWith("d")) {
                unit = 86_400_000L;
                text = text.substring(0, text.length() - 1);
            }
            return Math.max(Long.parseLong(text.trim()) * unit, MIN_INTERVAL_MS);
        } catch (NumberFormatException e) {
            log.warn("扫描间隔配置无效：{}，使用默认 15 分钟", raw);
            return DEFAULT_INTERVAL_MS;
        }
    }

    private void schedulePending() {
        if (pendingOpus.isEmpty() || scheduler.isShutdown()) {
            return;
        }
        ScheduledFuture<?> current = pendingEvent.get();
        if (current != null && !current.isDone() && !current.isCancelled()) {
            return;
        }
        try {
            pendingEvent.set(scheduler.schedule(this::drainPending, EVENT_DEBOUNCE_MS, TimeUnit.MILLISECONDS));
        } catch (RejectedExecutionException ignored) {
            // 关闭中
        }
    }

    private void drainPending() {
        if (running.get()) {
            // 全量扫描结束后会再排一次
            schedulePending();
            return;
        }
        if (pendingOpus.isEmpty()) {
            return;
        }
        Map<BigInteger, Integer> batch = new HashMap<>(pendingOpus);
        batch.keySet().forEach(pendingOpus::remove);
        worker.execute(() -> batch.forEach(this::scanSingle));
    }

    private void scanSingle(BigInteger opusId, Integer source) {
        try {
            AniOpus opus = lightDao.loadById(AniOpus.class, opusId);
            if (opus == null || (opus.getIsDeleted() != null && opus.getIsDeleted() == 1)) {
                return;
            }
            Path dir = resolveOpusDir(opus);
            if (dir == null) {
                log.debug("单部扫描找不到作品目录：{} ({})", opus.getNameCn(), opusId);
                return;
            }
            mediaScanExecutor.scanOpus(opus, dir, source == null ? SOURCE_SCAN : source);
        } catch (Exception e) {
            log.error("单部扫描失败：{}", opusId, e);
        }
    }

    private Path resolveOpusDir(AniOpus opus) {
        if (StrUtil.isNotBlank(opus.getMediaDir())) {
            Path path = Paths.get(opus.getMediaDir());
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        if (StrUtil.isBlank(opus.getNameCn())) {
            return null;
        }
        String rawRoots = fileProp.getAnimationSavePath();
        if (StrUtil.isBlank(rawRoots)) {
            return null;
        }
        for (String rootText : rawRoots.split(",")) {
            if (StrUtil.isBlank(rootText)) {
                continue;
            }
            Path path = Paths.get(rootText.trim(), opus.getNameCn());
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        return null;
    }

    private void persistStatus() {
        try {
            redisService.save(RedisKeyConst.MEDIA_SCAN_STATUS, JSON.toJSONString(status));
        } catch (Exception e) {
            log.warn("扫描状态写缓存失败：{}", e.getMessage());
        }
    }
}
