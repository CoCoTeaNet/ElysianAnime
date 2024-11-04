package net.cocotea.elysiananime.test;

import cn.hutool.core.date.DateUtil;
import net.cocotea.elysiananime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.elysiananime.api.system.service.SysNotifyService;
import net.cocotea.elysiananime.common.constant.NotifyConst;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.LevelEnum;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.jobs.SysNotifyJobs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

@Import(scanPackages = {"net.cocotea.elysiananime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class SysNotifyServiceTest {

    @Inject
    SysNotifyService sysNotifyService;

    @Inject
    SysNotifyJobs sysNotifyJobs;

    @Test
    public void addNotify() throws Exception {
        SysNotifyAddDTO sysNotifyAddDTO = new SysNotifyAddDTO()
                .setTitle("【" + "宝可梦" + "】更新啦~~~")
                .setMemo("资源名：[Nekomoe kissaten&LoliHouse] Shikanoko Nokonoko Koshitantan - 05 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv")
                .setJumpUrl("1274397675727507456")
                .setNotifyTime(DateUtil.date().toTimestamp())
                .setLevel(LevelEnum.INFO.getCode())
                .setIsGlobal(IsEnum.Y.getCode())
                .setNotifyType(NotifyConst.OPUS_UPDATE);
        sysNotifyService.addNotify(sysNotifyAddDTO);
    }

    @Test
    public void sysNotifyJobs() throws BusinessException {
        sysNotifyJobs.scan();
    }

}
