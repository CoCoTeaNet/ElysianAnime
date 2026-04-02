package net.cocotea.elysiananime.api.system.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.elysiananime.api.system.model.vo.SysOverviewVO;
import net.cocotea.elysiananime.api.system.service.SysDashboardService;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.MenuTypeEnum;
import net.cocotea.elysiananime.common.service.RedisService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author CoCoTea
 */
@Component
public class SysDashboardServiceImpl implements SysDashboardService {
    
    @Db("db1")
    private SqlToyLazyDao sqlToyLazyDao;
    
    @Inject
    private RedisService redisService;

    @Override
    public List<SysOverviewVO> getCount() {
        List<SysOverviewVO> sysOverviewList = new ArrayList<>(4);

        HashMap<String, Object> paramsMap = MapUtil.newHashMap();
        paramsMap.put("isDeleted", IsEnum.N.getCode());
        long countUser = sqlToyLazyDao.getCount("select count(1) from sys_user where is_deleted=:isDeleted", paramsMap);
        sysOverviewList.add(new SysOverviewVO().setTitle("用户数量").setCount(countUser));

        paramsMap = MapUtil.newHashMap();
        paramsMap.put("isDeleted", IsEnum.N.getCode());
        paramsMap.put("isMenu", IsEnum.Y.getCode());
        paramsMap.put("menuType", MenuTypeEnum.MENU.getCode());
        long countMenu = sqlToyLazyDao.getCount("select count(1) from sys_menu where is_deleted=:isDeleted and is_menu = :isMenu and menu_type = :menuType", paramsMap);
        sysOverviewList.add(new SysOverviewVO().setTitle("菜单数量").setCount(countMenu));

        paramsMap = MapUtil.newHashMap();
        paramsMap.put("isDeleted", IsEnum.N.getCode());
        long countRole = sqlToyLazyDao.getCount("select count(1) from sys_role where is_deleted=:isDeleted", paramsMap);
        sysOverviewList.add(new SysOverviewVO().setTitle("角色数量").setCount(countRole));

        long countOnline = redisService.keys(
                String.format(RedisKeyConst.ONLINE_USER, "*")
        ).size();
        sysOverviewList.add(new SysOverviewVO().setTitle("在线用户").setCount(countOnline));
        return sysOverviewList;
    }

}
