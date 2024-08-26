package net.cocotea.janime;

import cn.hutool.core.date.DateUtil;
import net.cocotea.janime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.janime.api.system.service.SysNotifyService;
import net.cocotea.janime.common.constant.NotifyConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.enums.LevelEnum;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.jobs.SysNotifyJobs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

@Import(scanPackages = {"net.cocotea.janime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class SysNotifyServiceTest {

    @Inject
    private SysNotifyService sysNotifyService;

    @Inject
    private SysNotifyJobs sysNotifyJobs;

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
