package net.cocotea.elysiananime.api.anime.rss;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.cocotea.elysiananime.api.anime.media.MediaScanner;
import net.cocotea.elysiananime.api.anime.model.dto.AniAddOpusTorrentDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniRssDTO;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.model.vo.AniVideoVO;
import net.cocotea.elysiananime.api.anime.model.vo.QuickSubscribeVO;
import net.cocotea.elysiananime.api.anime.model.vo.RssCountVO;
import net.cocotea.elysiananime.api.anime.rss.model.MkXmlDetail;
import net.cocotea.elysiananime.api.anime.rss.model.RenameInfo;
import net.cocotea.elysiananime.api.anime.service.AniOpusService;
import net.cocotea.elysiananime.api.anime.service.AniSpiderService;
import net.cocotea.elysiananime.common.constant.CharConst;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.RssStatusEnum;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.service.RedisService;
import net.cocotea.elysiananime.properties.DefaultProp;
import net.cocotea.elysiananime.properties.QbittorrentProp;
import net.cocotea.elysiananime.util.QbApiUtils;
import net.cocotea.elysiananime.util.ResUtils;
import net.cocotea.elysiananime.api.anime.rss.model.MkXmlItem;
import net.cocotea.elysiananime.api.anime.rss.model.QbInfo;
import net.cocotea.elysiananime.util.RuleUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.cocotea.elysiananime.common.constant.RedisKeyConst.RSS_RESULT_CACHE;

/**
 * 蜜柑rss订阅服务
 *
 * @author CoCoTea
 * @since v1
 */
@Component
public class MiKanRss {
    private static final Logger log = LoggerFactory.getLogger(MiKanRss.class);

    /**
     * 拉取 RSS 的超时时间，避免第三方站点无响应时把接口一起拖住
     */
    private static final int RSS_HTTP_TIMEOUT_MS = 15_000;

    @Inject
    private DefaultProp defaultProp;

    @Inject
    private QbApiUtils qbApiUtils;

    @Inject
    private QbittorrentProp qbittorrentProp;

    @Inject
    private AniOpusService aniOpusService;

    @Inject
    private AniSpiderService aniSpiderService;

    @Inject
    private ResUtils resUtils;

    @Inject
    private RedisService redisService;

    @Inject
    private MediaScanner mediaScanner;

    @Db
    private LightDao lightDao;

    /**
     * 保存并订阅rss
     */
    public void requestRss(String rssUrl, AniOpus opus) {
        String dir = resUtils.findASS(2) + opus.getNameCn() + CharConst.LEFT_LINE;
        List<RenameInfo> list;
        try {
            list = getRenames(rssUrl, opus);
        } catch (BusinessException e) {
            log.error("requestRss >>>>> 获取 RSS 资源失败，作品：{}，错误：{}", opus.getNameCn(), e.getErrorMsg());
            return;
        }

        for (RenameInfo renameInfo : list) {
            boolean existRes = resUtils.exist(opus.getNameCn(), renameInfo.getRename());
            if (!existRes) {
                log.info("requestRss >>>>> saveDir={}", dir);
                String added = qbApiUtils.addNewTorrent(renameInfo.getEnclosureUrl(), dir, renameInfo.getRename());
                log.info("requestRss >>>>> {}下载请求完成，响应消息：{}", opus.getNameCn(), added);
            } else {
                log.warn("requestRss >>>>> 《{}》资源已存在,rename：{},title：{}",
                        opus.getNameCn(),
                        renameInfo.getRename(),
                        renameInfo.getTitle()
                );
            }
        }
    }

    private void doRemoveResourceIfExist(String opusDir) {
        JSONArray list = qbApiUtils.info("all");
        for (Object obj : list) {
            QbInfo qbInfo = BeanUtil.toBean(obj, QbInfo.class);

            File resFile = FileUtil.file(qbInfo.getContentPath());
            File dirFile = FileUtil.file(opusDir);
            log.info("doRemoveResourceIfExist >>>>> resFile:{},dirFile:{}", resFile.getPath(), dirFile.getPath());
            if (!resFile.getPath().startsWith(dirFile.getPath())) {
                continue;
            }

            // 暂停
            qbApiUtils.pause(qbInfo.getHash());
            // 移除
            qbApiUtils.delete(qbInfo.getHash(), true);
        }

        ThreadUtil.sleep(5, TimeUnit.SECONDS);
        File[] ls = FileUtil.ls(opusDir);
        for (File f : ls) {
            FileUtil.del(f);
        }
    }

    public List<RenameInfo> getRenames(AniRssDTO aniRssDTO) throws BusinessException {
        AniOpus opus = aniOpusService.loadById(aniRssDTO.getId());
        if (opus == null) {
            throw new BusinessException("作品不存在或已被删除");
        }
        // rss订阅配置参数
        opus.setRssUrl(aniRssDTO.getRssUrl());
        opus.setRssLevelIndex(aniRssDTO.getRssLevelIndex());
        opus.setRssFileType(aniRssDTO.getRssFileType());
        opus.setRssOnlyMark(aniRssDTO.getRssOnlyMark());
        if (StrUtil.isNotBlank(aniRssDTO.getRssExcludeRes())) {
            opus.setRssExcludeRes(aniRssDTO.getRssExcludeRes());
        }
        // 预览不要求资源目录已存在，首次订阅前也能看到重命名结果
        return getRenames(aniRssDTO.getRssUrl(), opus, false);
    }

    public List<RenameInfo> getRenames(String rssUrl, AniOpus opus) throws BusinessException {
        return getRenames(rssUrl, opus, true);
    }

    private List<RenameInfo> getRenames(String rssUrl, AniOpus opus, boolean requireDir) throws BusinessException {
        String cacheKey = StrUtil.format(RSS_RESULT_CACHE, rssUrl);
        String cacheContent = redisService.get(cacheKey);
        String result = cacheContent;
        if (StrUtil.isBlank(result)) {
            result = fetchRssContent(rssUrl);
        }

        log.debug("订阅地址返回结果，result：{}", result);
        List<MkXmlItem> mkXmlItemList = parseXmlItems(result);
        if (StrUtil.isBlank(cacheContent)) {
            // 解析成功后再写缓存，避免把错误页面缓存下来
            redisService.saveByHours(cacheKey, result, 2);
        }
        List<RenameInfo> renameInfoList = new ArrayList<>();

        int levelIndex = opus.getRssLevelIndex() == null ? 0 : opus.getRssLevelIndex();
        String fileType = StrUtil.blankToDefault(opus.getRssFileType(), "mp4");

        for (MkXmlItem mkXmlItem : mkXmlItemList) {
            String title = mkXmlItem.getTitle();
            if (StrUtil.isBlank(title)) {
                continue;
            }
            // 是标识的资源
            boolean remarkRes = RuleUtils.isRemarkRes(title, opus.getRssOnlyMark());
            if (!remarkRes) {
                log.warn("解析完成 >>>>> 不是标识资源，不进行下载, title={}, mark={}", title, opus.getRssOnlyMark());
                continue;
            }
            if (requireDir && !resUtils.existDir(opus.getNameCn())) {
                log.warn("解析完成 >>>>> 目录不存在,opus：{}", opus.getNameCn());
                continue;
            }
            // 排除的资源
            if (StrUtil.isNotBlank(opus.getRssExcludeRes())) {
                boolean excludeRes = RuleUtils.isExcludeRes(title, opus.getRssExcludeRes());
                if (excludeRes) {
                    log.warn("解析结果 >>>>> 标识为排除的资源，不进行下载, title：{}, mark：{}", title, opus.getRssExcludeRes());
                    continue;
                }
            }
            // 资源是否存在（标题数字不足时跳过该条，避免整个订阅失败）
            String rename;
            try {
                rename = RuleUtils.rename(title, levelIndex, fileType);
            } catch (IndexOutOfBoundsException e) {
                log.warn("解析结果 >>>>> 标题中找不到第 {} 个数字，跳过, title：{}", levelIndex, title);
                continue;
            }

            RenameInfo renameInfo = new RenameInfo()
                    .setRename(rename).setTitle(title).setEnclosureUrl(mkXmlItem.getEnclosureUrl());
            renameInfoList.add(renameInfo);
        }

        return renameInfoList;
    }

    /**
     * 解析rss xml内容
     *
     * @param s xml字符串
     */
    public List<MkXmlItem> doParseXmlItems(String s) {
        Document xml = XmlUtil.parseXml(s);
        NodeList list = xml.getElementsByTagName("item");

        List<MkXmlItem> ls = new ArrayList<>(list.getLength());

        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            NodeList childNodes = item.getChildNodes();
            MkXmlItem mkXmlItem = new MkXmlItem();
            // 查找xml节点信息
            findItemInfo(childNodes, mkXmlItem);
            ls.add(mkXmlItem);
        }

        return ls;
    }

    public void findItemInfo(NodeList nodeList, MkXmlItem mkXmlItem) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String nodeName = item.getNodeName();
            switch (nodeName) {
                case "guid":
                    mkXmlItem.setGuid(item.getTextContent());
                    break;
                case "link":
                    mkXmlItem.setLink(item.getTextContent());
                    break;
                case "title":
                    mkXmlItem.setTitle(item.getTextContent());
                    break;
                case "description":
                    mkXmlItem.setDescription(item.getTextContent());
                    break;
                case "enclosure":
                    NamedNodeMap map = item.getAttributes();
                    Node lengthNode = map.getNamedItem("length");
                    if (lengthNode != null) {
                        mkXmlItem.setEnclosureLength(lengthNode.getNodeValue());
                    }
                    Node urlNode = map.getNamedItem("url");
                    if (urlNode != null) {
                        mkXmlItem.setEnclosureUrl(urlNode.getNodeValue());
                    }
                    break;
                case "torrent":
                    NodeList list = item.getChildNodes();
                    findBtSource(list, mkXmlItem);
                    break;
            }
        }
    }

    public void findBtSource(NodeList nodeList, MkXmlItem mkXmlItem) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String nodeName = item.getNodeName();
            switch (nodeName) {
                case "link":
                    mkXmlItem.setTorrentLink(item.getTextContent());
                    break;
                case "contentLength":
                    mkXmlItem.setTorrentContentLength(item.getTextContent());
                    break;
                case "pubDate":
                    mkXmlItem.setTorrentPubDate(item.getTextContent());
                    break;
            }
        }
    }

    /**
     * 获取订阅番剧并下载
     */
    public void doFindRssOpusAndDownload() {
        String methodName = "doFindRssOpusAndDownload >>> ";
        List<AniOpus> aniOpusList = aniOpusService.getRssOpus(RssStatusEnum.SUBSCRIBING.getCode());
        log.info("{}start...opusSize={}", methodName, aniOpusList.size());
        aniOpusList.forEach(opus -> {
            log.info("{}-------->开始下载", opus.getNameCn());
            String rssUrl = opus.getRssUrl();
            try {
                if (StrUtil.isNotBlank(rssUrl)) {
                    log.info("{}rssUrl: {}", methodName, rssUrl);
                    requestRss(rssUrl, opus);
                } else {
                    log.warn("没有订阅地址, opus={}", opus);
                }
            } catch (Exception e) {
                log.error("下载出现异常，作品是:{}, 堆栈信息:{}", opus.getNameCn(), e.getMessage());
            } finally {
                ThreadUtil.sleep(3000L);
            }
        });
    }

    /**
     * 订阅番剧
     *
     * @param aniRssDTO {@link AniRssDTO}
     * @return boolean
     */
    public boolean subscribe(AniRssDTO aniRssDTO) throws BusinessException {
        String baseMsg = "rss[subscribe]";
        log.info(baseMsg.concat("param={}"), aniRssDTO);
        AniOpus opus = aniOpusService.loadById(aniRssDTO.getId());
        if (opus == null) {
            throw new BusinessException("作品不存在或已被删除");
        }
        opus.setHasResource(IsEnum.Y.getCode());
        opus.setRssStatus(RssStatusEnum.SUBSCRIBING.getCode());
        // rss订阅配置参数
        opus.setRssUrl(aniRssDTO.getRssUrl());
        opus.setRssLevelIndex(aniRssDTO.getRssLevelIndex());
        opus.setRssFileType(aniRssDTO.getRssFileType());
        opus.setRssOnlyMark(aniRssDTO.getRssOnlyMark());
        if (StrUtil.isNotBlank(aniRssDTO.getRssExcludeRes())) {
            opus.setRssExcludeRes(aniRssDTO.getRssExcludeRes());
        }
        // 创建订阅资源目录
        String path = resUtils.findASS(2) + opus.getNameCn();
        log.info(baseMsg.concat("path={}"), path);
        FileUtil.mkdir(path);
        // 重新刷新一下rss订阅
        if (StrUtil.isNotBlank(opus.getRssUrl())) {
            ThreadUtil.execAsync(() -> {
                try {
                    // 覆盖更新
                    if (IsEnum.Y.getCode().equals(aniRssDTO.getRssOverride())) {
                        String dir = path + CharConst.LEFT_LINE;
                        File[] files = FileUtil.ls(dir);
                        if (files.length > 0) {
                            doRemoveResourceIfExist(dir);
                        }
                    }
                    requestRss(opus.getRssUrl(), opus);
                } catch (Exception e) {
                    log.error("异步订阅下载失败，作品：{}，错误：{}", opus.getNameCn(), e.getMessage(), e);
                }
            });
        }
        return aniOpusService.update(opus);
    }

    /**
     * rss订阅开关控制
     *
     * @param opusId 作品id
     * @return true
     */
    public boolean closeSubscribe(BigInteger opusId) {
        AniOpus aniOpus = new AniOpus().setId(opusId).setRssStatus(RssStatusEnum.SUBSCRIPTION_COMPLETED.getCode());
        return aniOpusService.update(aniOpus);
    }

    /**
     * 拉取 RSS 内容，网络异常统一转成业务异常，避免前端只看到“系统异常”
     */
    private String fetchRssContent(String rssUrl) throws BusinessException {
        if (StrUtil.isBlank(rssUrl)) {
            throw new BusinessException("RSS 订阅链接为空");
        }
        String result;
        try {
            result = HttpUtil.get(rssUrl, RSS_HTTP_TIMEOUT_MS);
        } catch (Exception e) {
            log.warn("请求RSS地址失败，url={}, error={}", rssUrl, e.getMessage());
            throw new BusinessException("RSS 地址访问失败，请检查链接或网络");
        }
        if (StrUtil.isBlank(result)) {
            throw new BusinessException("RSS 地址没有返回内容，请检查链接是否有效");
        }
        return result;
    }

    private List<MkXmlItem> parseXmlItems(String content) throws BusinessException {
        List<MkXmlItem> items;
        try {
            items = doParseXmlItems(content);
        } catch (Exception e) {
            log.warn("RSS内容解析失败，error={}", e.getMessage());
            throw new BusinessException("RSS 内容解析失败，请确认链接返回的是有效的 RSS");
        }
        if (items.isEmpty()) {
            throw new BusinessException("RSS 内容为空，请确认链接指向有效的番剧订阅");
        }
        return items;
    }

    /**
     * 快速订阅准备：从蜜柑 RSS 链接解析 bangumiId，自动补齐番剧信息并返回 RSS 解析结果
     *
     * @param rssUrl 蜜柑番剧 RSS 链接
     * @return {@link QuickSubscribeVO}
     */
    public QuickSubscribeVO quickPrepare(String rssUrl) throws BusinessException {
        if (StrUtil.isBlank(rssUrl)) {
            throw new BusinessException("请填写 RSS 订阅链接");
        }
        String bangumiId = ReUtil.get("(?i)[?&]bangumiId=(\\d+)", rssUrl.trim(), 1);
        if (StrUtil.isBlank(bangumiId)) {
            throw new BusinessException("链接中未找到 bangumiId，请粘贴蜜柑番剧的 RSS 链接");
        }
        String subjectId = resolveBgmSubjectId(rssUrl.trim(), bangumiId);
        String detailUrl = "/subject/" + subjectId;
        Map<String, Object> params = new HashMap<>(1);
        params.put("detailUrl", detailUrl);
        AniOpus opus = lightDao.findOne("ani_opus_findList", params, AniOpus.class);
        boolean existed = opus != null;
        if (!existed) {
            BigInteger opusId = aniSpiderService.addOpusFromBangumi("https://bgm.tv/subject/" + subjectId, IsEnum.N.getCode().intValue());
            opus = aniOpusService.loadById(opusId);
        }
        if (opus == null) {
            throw new BusinessException("番剧信息创建失败，请稍后重试");
        }
        MkXmlDetail detail = doParseMkXmlDetail(rssUrl.trim());
        return new QuickSubscribeVO()
                .setOpusId(opus.getId())
                .setNameCn(opus.getNameCn())
                .setNameOriginal(opus.getNameOriginal())
                .setCoverUrl(opus.getCoverUrl())
                .setDetailInfoUrl(opus.getDetailInfoUrl())
                .setExisted(existed)
                .setRssStatus(opus.getRssStatus())
                .setRssLevelIndex(opus.getRssLevelIndex())
                .setRssFileType(opus.getRssFileType())
                .setRssOnlyMark(opus.getRssOnlyMark())
                .setRssExcludeRes(opus.getRssExcludeRes())
                .setDetail(detail);
    }

    /**
     * 从蜜柑番剧页面解析对应的 Bangumi 条目ID
     */
    private String resolveBgmSubjectId(String rssUrl, String bangumiId) throws BusinessException {
        String host;
        try {
            host = URI.create(rssUrl).getHost();
        } catch (Exception e) {
            host = null;
        }
        if (StrUtil.isBlank(host)) {
            throw new BusinessException("RSS 链接格式有误");
        }
        String pageUrl = "https://" + host + "/Home/Bangumi/" + bangumiId;
        String html;
        try {
            html = HttpUtil.get(pageUrl, RSS_HTTP_TIMEOUT_MS);
        } catch (Exception e) {
            log.warn("quickPrepare >>> 请求蜜柑番剧页面失败, url={}, error={}", pageUrl, e.getMessage());
            throw new BusinessException("无法访问蜜柑番剧页面，请检查链接或网络");
        }
        String subjectId = ReUtil.get("(?i)(?:bgm\\.tv|bangumi\\.tv)/subject/(\\d+)", html, 1);
        if (StrUtil.isBlank(subjectId)) {
            throw new BusinessException("未能从蜜柑页面解析到 Bangumi 作品，请先手动添加番剧");
        }
        return subjectId;
    }

    /**
     * 解析资源信息详细
     *
     * @param rssUrl rss地址
     * @return {@link MkXmlDetail}
     */
    public MkXmlDetail doParseMkXmlDetail(String rssUrl) throws BusinessException {
        List<MkXmlItem> mkXmlItems = parseXmlItems(fetchRssContent(rssUrl));
        // 标题碎片
        List<List<String>> titleFragmentList = new ArrayList<>();
        // 集数索引
        List<List<String>> episodeIndexList = new ArrayList<>();
        for (MkXmlItem item : mkXmlItems) {
            String title = StrUtil.nullToEmpty(item.getTitle());
            String[] split = title.split("[\\[\\]/@#$%^&*()（）【】\\s]");
            List<String> titleFragment = Arrays.stream(split).filter(str -> !str.isEmpty()).collect(Collectors.toList());
            titleFragmentList.add(titleFragment);
            List<String> episodeIndex = ReUtil.findAllGroup0(RegexPool.NUMBERS, title);
            episodeIndexList.add(episodeIndex);
            // 将数字字符高亮显示
            String titleHtml = title;
            List<String> list = ReUtil.findAllGroup0(RegexPool.NUMBERS, titleHtml);
            for (String episodeStr : list) {
                titleHtml = titleHtml.replaceAll(episodeStr, "<span style=\"color:#f38181\">" + episodeStr + "</span>");
            }
            item.setTitle(title);
            item.setTitleHtml(titleHtml);
        }
        return new MkXmlDetail()
                .setItemList(mkXmlItems)
                .setTitleFragmentList(titleFragmentList)
                .setEpisodeIndexList(episodeIndexList);
    }

    public JSONObject getRssWorkStatus() {
        return Optional
                .of(redisService.get(RedisKeyConst.RSS_WORKS_STATUS))
                .map(JSONObject::parseObject)
                .orElse(new JSONObject());
    }

    public void doRenameBtV2() {
        String fileSeparator = File.separator;
        JSONArray completedArr = qbApiUtils.info("completed");
        JSONArray seedingArr = qbApiUtils.info("seeding");
        completedArr.fluentAddAll(seedingArr);

        for (Object obj : completedArr) {
            QbInfo info = BeanUtil.toBean(obj, QbInfo.class);
            log.info("doRenameBtV2 >>>>> QbInfo: {}", info.toString());
            try {
                String contentPath = info.getContentPath();
                File file = new File(contentPath);
                if (!file.exists()) {
                    log.warn("doRenameBtV2 >>>>> 文件未找到，删除下载记录，info: {}", info);
                    qbApiUtils.delete(info.getHash());
                    continue;
                }
                // 用qb工具重命名操作（做种的时候qb占用资源没法外部操作）
                String relativePath = StrUtil.replace(info.getContentPath(), info.getSavePath() + fileSeparator, "");
                String msg = qbApiUtils.renameFile(info.getHash(), info.getName(), relativePath);
                log.info("doRenameBtV2 >>>>> call qb renameFile api, msg: {}", msg);
                // 通知
                String opusName = FileUtil.file(info.getSavePath()).getName();
                AniOpus aniOpus = aniOpusService.loadByNameCn(opusName);
                // 下载完成后触发该作品的媒体扫描
                if (aniOpus != null) {
                    mediaScanner.scanOpusAsync(aniOpus.getId(), MediaScanner.SOURCE_RSS);
                }

            } catch (Exception ex) {
                log.error("doRenameBtV2 >>>>> 重命名失败，作品：{}，errorMsg：{}", info.getName(), ex.getMessage(), ex);
            }
        }
    }

    /**
     * 更新已订阅完成的作品
     */
    public void doCloseSubscribe() {
        aniOpusService.getRssOpus(RssStatusEnum.SUBSCRIBING.getCode())
                .forEach(aniOpus -> {
                    String folder = resUtils.findMediaDir(aniOpus.getNameCn());
                    File[] files = FileUtil.ls(folder);
                    if (files == null) {
                        log.warn("doCloseSubscribe >>> files is null");
                        return;
                    }

                    Map<String, List<AniVideoVO.Media>> medias = aniOpusService.getMedias(files);
                    List<AniVideoVO.Media> mediaList = medias.get("mediaList");
                    boolean equalled = ObjUtil.equal(String.valueOf(mediaList.size()), aniOpus.getEpisodes());
                    log.info("doCloseSubscribe >>> [{}]files: {}/{}, flag: {}", aniOpus.getNameCn(), files.length, aniOpus.getEpisodes(), equalled);

                    if (equalled) {
                        AniOpus updatePO = new AniOpus().setId(aniOpus.getId())
                                .setRssStatus(RssStatusEnum.SUBSCRIPTION_COMPLETED.getCode());
                        boolean updated = aniOpusService.update(updatePO);
                        log.info("doCloseSubscribe >>> 更新RSS状态值[{}]", updated);
                    }
                });
    }

    /**
     * 清除历史下载
     */
    public void clearHistory() {
        if (!defaultProp.getSeedFlag()) {
            JSONArray seeding = qbApiUtils.info("seeding");
            for (Object obj : seeding) {
                QbInfo qbInfo = BeanUtil.toBean(obj, QbInfo.class);
                qbApiUtils.pause(qbInfo.getHash());
            }
        }

        ThreadUtil.sleep(30, TimeUnit.SECONDS);

        // 查找已完成的种子
        JSONArray completed = qbApiUtils.info("completed");
        for (Object obj : completed) {
            QbInfo qbInfo = BeanUtil.toBean(obj, QbInfo.class);
            qbApiUtils.delete(qbInfo.getHash());
        }
    }

    public String addOpusTorrent(AniAddOpusTorrentDTO opusTorrentDTO) throws BusinessException {
        AniOpus aniOpus = aniOpusService.loadById(new BigInteger(opusTorrentDTO.getOpusId()));
        Assert.isFalse(aniOpus == null, () -> new BusinessException("找不到这个作品"));

        String mediaDir = resUtils.findMediaDir(aniOpus.getNameCn());
        if (!FileUtil.exist(mediaDir)) {
            FileUtil.mkdir(mediaDir);
        }

        if (StrUtil.isNotBlank(opusTorrentDTO.getFileType())) {
            String rename = Opt.ofNullable(opusTorrentDTO.getEpisodes())
                    .map(String::valueOf)
                    .orElseGet(() -> aniOpus.getNameCn() + "_RES");
            rename += rename + "." + opusTorrentDTO.getFileType();
            return addTorrent(opusTorrentDTO.getTorrentUrl(), mediaDir, rename);
        }

        return addTorrent(opusTorrentDTO.getTorrentUrl(), mediaDir, null);
    }

    /**
     * 下载器未配置或不可用时转成业务异常，避免前端只看到“系统异常”
     */
    private String addTorrent(String torrentUrl, String mediaDir, String rename) throws BusinessException {
        try {
            return qbApiUtils.addNewTorrent(torrentUrl, mediaDir, rename);
        } catch (IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public RssCountVO getCounts() {
        // 定义中的数量
        Map<String, Object> params = MapUtil.builder(new HashMap<String, Object>())
                .put("rssStatus", RssStatusEnum.SUBSCRIBING.getCode())
                .build();
        Long subscribing = lightDao.getCount("ani_opus_count", params);
        // 定义完成的数量
        params.replace("rssStatus", RssStatusEnum.SUBSCRIPTION_COMPLETED.getCode());
        Long subscriptionCompleted = lightDao.getCount("ani_opus_count", params);
        return new RssCountVO().setSubscribing(subscribing)
                .setSubscriptionCompleted(subscriptionCompleted);
    }

}
