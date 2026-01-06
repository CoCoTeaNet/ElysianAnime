package net.cocotea.elysiananime.jobs;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.elysiananime.api.anime.model.dto.RssWorksStatusDTO;
import net.cocotea.elysiananime.api.anime.rss.MiKanRss;
import net.cocotea.elysiananime.api.anime.service.AniOpusService;
import net.cocotea.elysiananime.api.anime.service.AniSpiderService;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.service.RedisService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

/**
 * rss订阅调度服务
 *
 * @author CoCoTea
 * @version  v2.0.0
 */
@Component
@Slf4j
public class OpusJobs {

    @Inject
    private RedisService redisService;

    @Inject
    private MiKanRss miKanRss;

    @Inject
    private AniSpiderService aniSpiderService;

    @Inject
    private AniOpusService aniOpusService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void scanBt() {
        // 定时重命名
        miKanRss.doRenameBtV2();
        // 清理历史记录
        miKanRss.clearHistory();
        miKanRss.doCloseSubscribe();
    }

    /**
     * 定时自动抓取番剧信息
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24 * 7, initialDelay = 15000)
    public void autoFetchOpus() {
        try {
            aniSpiderService.autoFetchOpus();
        } catch (BusinessException ex) {
            log.error("autoFetchOpus >>> error,msg={}", ex.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0/6 * * ?")
    public void scanRss() {
        RssWorksStatusDTO worksStatusDTO = new RssWorksStatusDTO().setLastExecTime(DateUtil.now());
        // 获取订阅番剧并下载
        try {
            worksStatusDTO.setExecMessage("执行ing...");
            miKanRss.doFindRssOpusAndDownload();
            worksStatusDTO.setExecMessage("执行完成！！！");
        } catch (Exception ex) {
            worksStatusDTO.setExecMessage("运行异常：" + ex.getMessage());
            log.error("scanRss >>> error,msg={}", ex.getMessage());
        }
        redisService.save(RedisKeyConst.RSS_WORKS_STATUS, JSONUtil.toJsonStr(worksStatusDTO));
    }

    /**
     * 定时发现目录下的资源
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24, initialDelay = 25000)
    public void autoDiscoverResource() {
        aniOpusService.autoDiscoverResource();
    }

}
