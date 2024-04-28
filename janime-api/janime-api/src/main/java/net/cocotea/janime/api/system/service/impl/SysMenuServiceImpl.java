package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.system.model.dto.SysMenuAddDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuPageDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysMenu;
import net.cocotea.janime.api.system.model.po.SysRoleMenu;
import net.cocotea.janime.api.system.model.po.SysUserRole;
import net.cocotea.janime.api.system.model.vo.SysMenuVO;
import net.cocotea.janime.api.system.service.SysMenuService;
import net.cocotea.janime.common.constant.CharConst;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.service.RedisService;
import net.cocotea.janime.common.util.TreeBuilder;
import net.cocotea.janime.util.LoginUtils;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CoCoTea
 * @since 2022-11-28 17:51:41
 */
@Service
public class SysMenuServiceImpl implements SysMenuService {
    private static final Logger logger = LoggerFactory.getLogger(SysMenuServiceImpl.class);

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Resource
    private RedisService redisService;

    @Override
    public boolean add(SysMenuAddDTO param) {
        SysMenu sysMenu = sqlToyHelperDao.convertType(param, SysMenu.class);
        // 菜单
        if (StringUtil.isBlank(sysMenu.getParentId())) {
            sysMenu.setParentId(BigInteger.ZERO);
        }
        // 权限
        if (StringUtil.isBlank(sysMenu.getPermissionCode()) && StringUtil.isNotBlank(sysMenu.getRouterPath())) {
            sysMenu.setPermissionCode(sysMenu.getRouterPath().replace(CharConst.LEFT_LINE, CharConst.COLON));
        }
        Object menuId = sqlToyHelperDao.save(sysMenu);
        return menuId != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public ApiPage<SysMenuVO> listByPage(SysMenuPageDTO pageDTO) {
        Page<SysMenu> page = sqlToyHelperDao.findPage(baseQueryWrapper(pageDTO.getPO()), new Page<SysMenu>());
        return ApiPage.rest(page, SysMenuVO.class);
    }

    @Override
    public List<SysMenuVO> listByTree(SysMenuPageDTO pageDTO) {
        LambdaQueryWrapper<SysMenu> wrapper = baseQueryWrapper(pageDTO.getPO());
        List<SysMenuVO> list = Convert.toList(SysMenuVO.class, sqlToyHelperDao.findList(wrapper));
        return new TreeBuilder<SysMenuVO>().get(list);
    }

    @Override
    public boolean update(SysMenuUpdateDTO param) {
        SysMenu sysMenu = sqlToyHelperDao.convertType(param, SysMenu.class);
        Long update = sqlToyHelperDao.update(sysMenu);
        return update > 0;
    }

    @Override
    public List<SysMenuVO> listByUserId(Integer isMenu) {
        // 1、获取登录用户ID
        BigInteger loginId = LoginUtils.loginId();
        // 2、查询用户的角色
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = new LambdaQueryWrapper<>(SysUserRole.class)
                .select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, loginId);
        List<BigInteger> roleIds = sqlToyHelperDao
                .findList(userRoleWrapper).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        // 3、查询角色含有的菜单
        LambdaQueryWrapper<SysRoleMenu> menuRoleWrapper = new LambdaQueryWrapper<>(SysRoleMenu.class)
                .select(SysRoleMenu::getMenuId)
                .in(SysRoleMenu::getRoleId, roleIds);
        List<BigInteger> menuIds = sqlToyHelperDao
                .findList(menuRoleWrapper).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        // 4、根据菜单ID查询菜单信息
        LambdaQueryWrapper<SysMenu> menuWrapper = new LambdaQueryWrapper<>(SysMenu.class)
                .select(SysMenu::getId).select(SysMenu::getParentId).select(SysMenu::getMenuName)
                .select(SysMenu::getIconPath).select(SysMenu::getIsExternalLink)
                .eq(SysMenu::getIsMenu, isMenu)
                .in(SysMenu::getId, menuIds)
                .orderByDesc(SysMenu::getSort)
                .orderByDesc(SysMenu::getId);
        if (isMenu == IsEnum.Y.getCode().intValue()) {
            menuWrapper.select(SysMenu::getRouterPath);
        } else {
            menuWrapper.select(SysMenu::getPermissionCode);
        }
        List<SysMenu> list = sqlToyHelperDao.findList(menuWrapper);
        return Convert.toList(SysMenuVO.class, list);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(BigInteger id) {
        SysMenu sysMenu = new SysMenu().setId(id).setIsDeleted(IsEnum.Y.getCode());
        Long update = sqlToyHelperDao.update(sysMenu);
        if (update <= 0) {
            return false;
        }
        // 获取子节点
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>(SysMenu.class)
                .select()
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getIsDeleted, IsEnum.N.getCode());
        List<SysMenu> sysMenuList = sqlToyHelperDao.findList(wrapper);
        if (!sysMenuList.isEmpty()) {
            // 存在子节点，删除子节点
            sysMenuList.forEach(item -> delete(item.getId()));
        }
        return true;
    }

    @Override
    public List<SysMenuVO> listByRoleId(String roleId) {
        // 查找角色关联的菜单
        LambdaQueryWrapper<SysRoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>(SysRoleMenu.class)
                .select()
                .in(SysRoleMenu::getRoleId, roleId);
        List<BigInteger> menuIds = sqlToyHelperDao
                .findList(roleMenuWrapper).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        // 查找菜单
        LambdaQueryWrapper<SysMenu> menuWrapper = new LambdaQueryWrapper<>(SysMenu.class)
                .select()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getIsDeleted, IsEnum.N.getCode())
                .orderByDesc(SysMenu::getSort)
                .orderByDesc(SysMenu::getId);
        List<SysMenu> list = sqlToyHelperDao.findList(menuWrapper);
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
    public List<SysMenuVO> listByTreeAsRoleSelection(SysMenuPageDTO pageDTO) {
        List<SysMenu> list = sqlToyHelperDao.findList(baseQueryWrapper(pageDTO.getPO()));
        List<SysMenuVO> sysMenuVOList = Convert.toList(SysMenuVO.class, list);
        return new TreeBuilder<SysMenuVO>().get(sysMenuVOList);
    }

    private LambdaQueryWrapper<SysMenu> baseQueryWrapper(SysMenu sysMenu) {
        return new LambdaQueryWrapper<>(SysMenu.class)
                .select()
                .eq(SysMenu::getIsDeleted, IsEnum.N.getCode())
                .eq(SysMenu::getIsMenu, sysMenu.getIsMenu())
                .eq(SysMenu::getPermissionCode, sysMenu.getPermissionCode())
                .like(SysMenu::getMenuName, sysMenu.getMenuName())
                .eq(SysMenu::getMenuStatus, sysMenu.getMenuStatus())
                .orderByDesc(SysMenu::getSort)
                .orderByDesc(SysMenu::getId);
    }

}
