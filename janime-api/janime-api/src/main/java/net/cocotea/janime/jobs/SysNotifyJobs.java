package net.cocotea.janime.jobs;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.janime.api.system.model.po.SysNotify;
import net.cocotea.janime.api.system.model.po.SysUser;
import net.cocotea.janime.api.system.service.SysNotifyService;
import net.cocotea.janime.common.enums.AccountStatusEnum;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.BusinessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SysNotifyJobs {

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Resource
    private SysNotifyService sysNotifyService;

    @Scheduled(initialDelay = 1L, fixedDelay = 60L, timeUnit = TimeUnit.SECONDS)
    void scan() throws BusinessException {
        // 将全局消息转换成每个接收人
        DateTime now = DateUtil.date();
        DateTime start = DateUtil.offsetDay(now, -30);
        // 查询全局消息
        LambdaQueryWrapper<SysNotify> sysNotifyWrapper = Wrappers.lambdaWrapper(SysNotify.class)
                .between(SysNotify::getNotifyTime, start, now)
                .eq(SysNotify::getIsDeleted, IsEnum.N.getCode())
                .eq(SysNotify::getIsGlobal, IsEnum.Y.getCode());
        List<SysNotify> sysNotifyList = sqlToyHelperDao.findList(sysNotifyWrapper);
        // 查询所有用户
        LambdaQueryWrapper<SysUser> sysUserWrapper = Wrappers.lambdaWrapper(SysUser.class)
                .eq(SysUser::getIsDeleted, IsEnum.N.getCode())
                .eq(SysUser::getAccountStatus, AccountStatusEnum.NORMAL.getCode());
        List<SysUser> sysUserList = sqlToyHelperDao.findList(sysUserWrapper);
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
            sqlToyHelperDao.update(sysNotify);
        }
    }

}
