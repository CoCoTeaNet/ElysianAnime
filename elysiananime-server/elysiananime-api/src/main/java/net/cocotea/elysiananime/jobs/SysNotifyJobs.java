package net.cocotea.elysiananime.jobs;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import net.cocotea.elysiananime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.elysiananime.api.system.model.po.SysNotify;
import net.cocotea.elysiananime.api.system.model.po.SysUser;
import net.cocotea.elysiananime.api.system.service.SysNotifyService;
import net.cocotea.elysiananime.common.enums.AccountStatusEnum;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.model.BusinessException;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class SysNotifyJobs {

    private static final Logger log = LoggerFactory.getLogger(SysNotifyJobs.class);

    @Db
    private LightDao lightDao;

    @Inject
    private SysNotifyService sysNotifyService;

    @Scheduled(initialDelay = 1L, fixedDelay = 60L * 1000)
    public void scan() throws BusinessException {
        // 将全局消息转换成每个接收人
        DateTime now = DateUtil.date();
        DateTime start = DateUtil.offsetDay(now, -30);
        // 查询全局消息
        Map<String, Object> sysNotifyMapDTO = MapUtil.newHashMap(2);
        sysNotifyMapDTO.put("betweenNotifyTime", Arrays.asList(start, now));
        sysNotifyMapDTO.put("isGlobal", IsEnum.Y.getCode());
        List<SysNotify> sysNotifyList = lightDao.find("sys_notify_findList", sysNotifyMapDTO, SysNotify.class);
        log.info("scan >>>>> Number to be notified: {}", sysNotifyList.size());
        // 查询所有用户
        Map<String, Object> sysUserMapDTO = MapUtil.newHashMap(1);
        sysUserMapDTO.put("accountStatus", AccountStatusEnum.NORMAL.getCode());
        List<SysUser> sysUserList = lightDao.find("sys_user_findList", sysUserMapDTO, SysUser.class);
        // 转换通知
        for (SysNotify sysNotify : sysNotifyList) {
            for (SysUser sysUser : sysUserList) {
                SysNotifyAddDTO addDTO = Convert.convert(SysNotifyAddDTO.class, sysNotify);
                addDTO.setReceiver(sysUser.getId());
                addDTO.setIsGlobal(IsEnum.N.getCode());
                sysNotifyService.addNotify(addDTO);
            }
            // 转发完成后删除
            sysNotify.setIsDeleted(IsEnum.Y.getCode());
            lightDao.update(sysNotify);
            log.info("scan >>>>> send finish, title is {}", sysNotify.getTitle());
        }
    }

}
