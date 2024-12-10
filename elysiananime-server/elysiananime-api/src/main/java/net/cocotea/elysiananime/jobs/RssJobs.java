package net.cocotea.elysiananime.jobs;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import net.cocotea.elysiananime.api.anime.model.dto.RssWorksStatusDTO;
import net.cocotea.elysiananime.api.anime.rss.MiKanRss;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.service.RedisService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rss订阅调度服务
 *
 * @author CoCoTea
 * @version  v2.0.0
 */
@Component
public class RssJobs {

    private static final Logger logger = LoggerFactory.getLogger(RssJobs.class);

    @Inject
    private RedisService redisService;

    @Inject
    private MiKanRss miKanRss;

    /**
    // @Scheduled(cron = "0 0/5 * * * ?")
    public void scanBt() {
        // 定时暂停正在做种的
        try {
            miKanRss.doPauseSeedingBt();
        } catch (Exception e) {
            logger.error("doPauseSeedingBt >>>>> error,msg={}", e.getMessage());
        }
        // 定时重命名
        try {
            miKanRss.doRenameBt();
        } catch (BusinessException e) {
            logger.error("doRenameBt >>>>> error,msg={}", e.getMessage());
        }
    }
     */

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
            logger.error("scanRss >>>>> error,msg={}", ex.getMessage());
        }
        redisService.save(RedisKeyConst.RSS_WORKS_STATUS, JSONUtil.toJsonStr(worksStatusDTO));
    }

}
