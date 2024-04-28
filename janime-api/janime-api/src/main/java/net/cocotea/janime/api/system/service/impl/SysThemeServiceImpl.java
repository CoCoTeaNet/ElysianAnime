package net.cocotea.janime.api.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.system.model.dto.SysThemeUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysTheme;
import net.cocotea.janime.api.system.model.vo.SysThemeVO;
import net.cocotea.janime.api.system.service.SysThemeService;
import net.cocotea.janime.util.LoginUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * 系统主题表
 *
 * @author CoCoTea 572315466@qq.com
 * @version 2.0.0
 */
@Service
public class SysThemeServiceImpl implements SysThemeService {

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Override
    public boolean updateByUser(SysThemeUpdateDTO param) {
        BigInteger userId = LoginUtils.loginId();
        SysTheme sysTheme = Convert.convert(SysTheme.class, param);
        LambdaQueryWrapper<SysTheme> queryWrapper = Wrappers.lambdaWrapper(SysTheme.class)
                .eq(SysTheme::getUserId, userId);
        SysTheme loadSysTheme = sqlToyHelperDao.findOne(queryWrapper);
        if (loadSysTheme == null) {
            sysTheme.setLayoutMode(0);
            sysTheme.setUserId(userId);
            return sqlToyHelperDao.save(sysTheme) != null;
        } else {
            sysTheme.setId(loadSysTheme.getId());
            return sqlToyHelperDao.update(sysTheme) > 0;
        }
    }

    @Override
    public SysThemeVO loadByUser() {
        String userId = StpUtil.getLoginId().toString();
        LambdaQueryWrapper<SysTheme> queryWrapper = Wrappers.lambdaWrapper(SysTheme.class)
                .eq(SysTheme::getUserId, userId);
        SysTheme loadSysTheme = sqlToyHelperDao.findOne(queryWrapper);
        return Convert.convert(SysThemeVO.class, loadSysTheme);
    }
}