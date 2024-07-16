package net.cocotea.janime.jobs;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import net.cocotea.janime.api.anime.model.dto.RssWorksStatusDTO;
import net.cocotea.janime.api.anime.rss.MiKanRss;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * rss订阅调度服务
 *
 * @author CoCoTea
 * @version  v2.0.0
 */
@Component
public class RssJobs {

    private static final Logger logger = LoggerFactory.getLogger(RssJobs.class);

    @Resource
    private RedisService redisService;

    @Resource
    private MiKanRss miKanRss;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void scanBt() {
        // 定时暂停正在做种的
        try {
            miKanRss.doPauseSeedingBt();
        } catch (Exception e) {
            logger.error("doPauseSeedingBt-------->error,msg={}", e.getMessage());
        }
        // 定时重命名
        try {
            miKanRss.doRenameBt();
        } catch (BusinessException e) {
            logger.error("doRenameBt-------->error,msg={}", e.getMessage());
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
            logger.error("scanRss-------->error,msg={}", ex.getMessage());
        }
        try {
            redisService.save(RedisKeyConst.RSS_WORKS_STATUS, JSONUtil.toJsonStr(worksStatusDTO));
        } catch (Exception ex) {
            logger.error("scanRss-------->error,msg={}", ex.getMessage());
        }
    }

}
