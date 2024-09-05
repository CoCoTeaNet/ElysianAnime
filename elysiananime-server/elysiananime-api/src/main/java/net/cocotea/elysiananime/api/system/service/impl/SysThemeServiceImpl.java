package net.cocotea.elysiananime.api.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import net.cocotea.elysiananime.api.system.model.dto.SysThemeUpdateDTO;
import net.cocotea.elysiananime.api.system.model.po.SysTheme;
import net.cocotea.elysiananime.api.system.model.vo.SysThemeVO;
import net.cocotea.elysiananime.api.system.service.SysThemeService;
import net.cocotea.elysiananime.util.LoginUtils;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;

/**
 * 系统主题表
 *
 * @author CoCoTea 572315466@qq.com
 * @version 2.0.0
 */
@Component
public class SysThemeServiceImpl implements SysThemeService {

    @Db("db1")
    private SqlToyLazyDao sqlToyLazyDao;

    @Override
    public boolean updateByUser(SysThemeUpdateDTO param) {
        BigInteger userId = LoginUtils.loginId();
        SysTheme sysTheme = Convert.convert(SysTheme.class, param);
        SysTheme loadSysTheme = sqlToyLazyDao.loadBySql("select * from sys_theme where user_id = :userId", new SysTheme().setUserId(userId));
        if (loadSysTheme == null) {
            sysTheme.setLayoutMode(0);
            sysTheme.setUserId(userId);
            return sqlToyLazyDao.save(sysTheme) != null;
        } else {
            sysTheme.setId(loadSysTheme.getId());
            return sqlToyLazyDao.update(sysTheme) > 0;
        }
    }

    @Override
    public SysThemeVO loadByUser() {
        String userId = StpUtil.getLoginId().toString();
        return sqlToyLazyDao.loadBySql("select * from sys_theme where user_id = :userId", new SysThemeVO().setUserId(userId));
    }
}