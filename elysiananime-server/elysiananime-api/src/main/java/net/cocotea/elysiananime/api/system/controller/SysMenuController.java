package net.cocotea.elysiananime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.elysiananime.api.system.model.dto.SysMenuAddDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysMenuPageDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysMenuTreeDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysMenuUpdateDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysMenuVO;
import net.cocotea.elysiananime.api.system.service.SysMenuService;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统菜单或权限管理接口
 *
 * @author CoCoTea
 * @version v2.0.0
 */
@Controller
@Mapping("/system/menu")
public class SysMenuController {
    @Inject
    private SysMenuService sysMenuService;

    /**
     * 新增菜单或权限
     *
     * @param addDTO {@link SysMenuAddDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("add")
    public ApiResult<Boolean> add(@Validated @Body SysMenuAddDTO addDTO) throws BusinessException {
        boolean b = sysMenuService.add(addDTO);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除菜单或权限
     *
     * @param idList ID集合
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("deleteBatch")
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> idList) throws BusinessException {
        boolean b = sysMenuService.deleteBatch(idList);
        return ApiResult.ok(b);
    }

    /**
     * 更新菜单或权限信息
     *
     * @param updateDTO {@link SysMenuUpdateDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("update")
    public ApiResult<Boolean> update(@Validated @Body SysMenuUpdateDTO updateDTO) throws BusinessException {
        boolean b = sysMenuService.update(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 分页查询菜单或权限
     *
     * @param pageDTO {@link SysMenuPageDTO}
     * @return {@link ApiPage<SysMenuVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("listByPage")
    public ApiResult<ApiPage<SysMenuVO>> listByPage(@Validated @Body SysMenuPageDTO pageDTO) throws BusinessException {
        ApiPage<SysMenuVO> menus = sysMenuService.listByPage(pageDTO);
        return ApiResult.ok(menus);
    }

    /**
     * 查询菜单或权限树形数据
     *
     * @param pageDTO {@link SysMenuPageDTO}
     * @return {@link List<SysMenuVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("listByTree")
    public ApiResult<List<SysMenuVO>> listByTree(@Validated @Body SysMenuTreeDTO pageDTO) {
        List<SysMenuVO> menus = sysMenuService.listByTree(pageDTO);
        return ApiResult.ok(menus);
    }

    /**
     * 通关角色获取菜单或权限
     *
     * @param pageDTO {@link SysMenuPageDTO}
     * @return {@link List<SysMenuVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("listByTreeAsRoleSelection")
    public ApiResult<List<SysMenuVO>> listByTreeAsRoleSelection(@Validated @Body SysMenuTreeDTO pageDTO) {
        List<SysMenuVO> menus = sysMenuService.listByTreeAsRoleSelection(pageDTO);
        return ApiResult.ok(menus);
    }

    /**
     * 通关角色获取请所有菜单
     *
     * @param roleId 角色ID
     * @return {@link List<SysMenuVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Get
    @Mapping("listByRoleId/{roleId}")
    public ApiResult<List<SysMenuVO>> listByRoleId(@Path String roleId) {
        List<SysMenuVO> menus = sysMenuService.listByRoleId(roleId);
        return ApiResult.ok(menus);
    }
}
