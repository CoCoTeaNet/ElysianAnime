package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.json.JSONUtil;
import net.cocotea.janime.api.system.model.dto.SysMenuAddDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuPageDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuTreeDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysMenu;
import net.cocotea.janime.api.system.model.po.SysRoleMenu;
import net.cocotea.janime.api.system.model.po.SysUserRole;
import net.cocotea.janime.api.system.model.vo.SysMenuVO;
import net.cocotea.janime.api.system.service.SysMenuService;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.service.RedisService;
import net.cocotea.janime.common.util.TreeBuilder;
import net.cocotea.janime.util.LoginUtils;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.sagacity.sqltoy.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.noear.solon.annotation.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author CoCoTea
 * @since 2022-11-28 17:51:41
 */
@Component
public class SysMenuServiceImpl implements SysMenuService {
    private static final Logger logger = LoggerFactory.getLogger(SysMenuServiceImpl.class);

    @Db("db1")
    private SqlToyLazyDao sqlToyLazyDao;

    @Db("db1")
    private LightDao lightDao;

    @Inject
    private RedisService redisService;

    @Override
    public boolean add(SysMenuAddDTO param) {
        SysMenu sysMenu = sqlToyLazyDao.convertType(param, SysMenu.class);
        // 菜单
        if (StringUtil.isBlank(sysMenu.getParentId())) {
            sysMenu.setParentId(BigInteger.ZERO);
        }
        // 权限
        if (StringUtil.isBlank(sysMenu.getPermissionCode()) && StringUtil.isNotBlank(sysMenu.getRouterPath())) {
            sysMenu.setPermissionCode(sysMenu.getRouterPath().replace(CharPool.SLASH, CharPool.COLON));
        }
        Object menuId = sqlToyLazyDao.save(sysMenu);
        return menuId != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public ApiPage<SysMenuVO> listByPage(SysMenuPageDTO pageDTO) {
        Map<String, Object> map = BeanUtil.beanToMap(pageDTO.getSysMenu());
        Page<Object> fastPage = ApiPage.create(pageDTO);
        Page<SysMenu> page = lightDao.findPage(fastPage, "sys_menu_findList", map, SysMenu.class);
        return ApiPage.rest(page, SysMenuVO.class);
    }

    @Override
    public List<SysMenuVO> listByTree(SysMenuTreeDTO treeDTO) {
        Map<String, Object> map = BeanUtil.beanToMap(treeDTO);
        List<SysMenuVO> list = lightDao.find("sys_menu_findList", map, SysMenuVO.class);
        return new TreeBuilder<SysMenuVO>().get(list);
    }

    @Override
    public boolean update(SysMenuUpdateDTO param) {
        SysMenu sysMenu = sqlToyLazyDao.convertType(param, SysMenu.class);
        Long update = sqlToyLazyDao.update(sysMenu);
        return update > 0;
    }

    @Override
    public List<SysMenuVO> listByUserId(Integer isMenu) {
        // 1、获取登录用户ID
        BigInteger loginId = LoginUtils.loginId();
        // 2、查询用户的角色
        List<BigInteger> roleIds = sqlToyLazyDao.findBySql("sys_user_role_findList", new SysUserRole().setUserId(loginId)).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        // 3、查询角色含有的菜单
        Map<String, Object> sysRoleMenuMap = new HashMap<>(1);
        sysRoleMenuMap.put("roleIds", roleIds);
        List<BigInteger> menuIds = sqlToyLazyDao.findBySql("sys_role_menu_findList", sysRoleMenuMap, SysRoleMenu.class).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        // 4、根据菜单ID查询菜单信息
        Map<String, Object> sysMenuMap = new HashMap<>(2);
        sysMenuMap.put("menuIds", menuIds);
        sysMenuMap.put("isMenu", isMenu);
        List<SysMenu> list = sqlToyLazyDao.findBySql("sys_menu_findList", sysMenuMap, SysMenu.class);
        return Convert.toList(SysMenuVO.class, list);
    }

    @Tran
    @Override
    public boolean delete(BigInteger id) {
        SysMenu sysMenu = new SysMenu().setId(id).setIsDeleted(IsEnum.Y.getCode());
        Long update = sqlToyLazyDao.update(sysMenu);
        if (update <= 0) {
            return false;
        }
        // 获取子节点
        HashMap<String, Object> map = MapUtil.newHashMap(1);
        map.put("parentId", id);
        List<SysMenu> sysMenuList = sqlToyLazyDao.findBySql("sys_menu_findList", map, SysMenu.class);
        if (!sysMenuList.isEmpty()) {
            // 存在子节点，删除子节点
            sysMenuList.forEach(item -> delete(item.getId()));
        }
        return true;
    }

    @Override
    public List<SysMenuVO> listByRoleId(String roleId) {
        HashMap<String, Object> sysMenuMap = MapUtil.newHashMap(1);
        sysMenuMap.put("roleId", roleId);
        List<SysMenu> list = sqlToyLazyDao.findBySql("sys_menu_IN_findList", sysMenuMap, SysMenu.class);
        return Convert.toList(SysMenuVO.class, list);
    }

    @Override
    public List<SysMenuVO> cachePermission(BigInteger userId) {
        // 缓存权限
        List<SysMenuVO> permissions = listByUserId(IsEnum.N.getCode());
        redisService.save(String.format(RedisKeyConst.USER_PERMISSION, userId), JSONUtil.toJsonStr(permissions), 3600 * 24L);
        return permissions;
    }

    @Override
    public List<SysMenuVO> getCachePermission(BigInteger userId) {
        String s = redisService.get(String.format(RedisKeyConst.USER_PERMISSION, userId));
        logger.info("[{}]-permissions={}", userId, s);
        return JSONUtil.toList(s, SysMenuVO.class);
    }

    @Override
    public List<SysMenuVO> listByTreeAsRoleSelection(SysMenuTreeDTO treeDTO) {
        Map<String, Object> map = BeanUtil.beanToMap(treeDTO);
        List<SysMenuVO> list = lightDao.find("sys_menu_findList", map, SysMenuVO.class);
        return new TreeBuilder<SysMenuVO>().get(list);
    }

}
