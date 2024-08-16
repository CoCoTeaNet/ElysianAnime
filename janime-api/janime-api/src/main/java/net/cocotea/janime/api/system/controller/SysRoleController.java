package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.janime.api.system.model.dto.SysRoleAddDTO;
import net.cocotea.janime.api.system.model.dto.SysRolePageDTO;
import net.cocotea.janime.api.system.model.dto.SysRoleUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysRoleMenuVO;
import net.cocotea.janime.api.system.model.vo.SysRoleVO;
import net.cocotea.janime.api.system.service.SysRoleService;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统角色管理接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Mapping("/system/role")
@Controller
public class SysRoleController {

    @Inject
    private SysRoleService sysRoleService;

    /**
     * 新增角色
     *
     * @param addDTO {@link SysRoleAddDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("/add")
    public ApiResult<Boolean> add(@Validated @Body SysRoleAddDTO addDTO) throws BusinessException {
        boolean b = sysRoleService.add(addDTO);
        return ApiResult.ok(b);
    }

    /**
     * 更新角色信息
     *
     * @param updateDTO {@link SysRoleUpdateDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/update")
    public ApiResult<Boolean> update(@Validated @Body SysRoleUpdateDTO updateDTO) throws BusinessException {
        boolean b = sysRoleService.update(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/delete/{id}")
    public ApiResult<Boolean> delete(@Path BigInteger id) throws BusinessException {
        boolean b = sysRoleService.delete(id);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除角色
     *
     * @param idList 角色ID集合
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/deleteBatch")
    public ApiResult<Boolean> deleteBatch(@Body List<BigInteger> idList) throws BusinessException {
        boolean b = sysRoleService.deleteBatch(idList);
        return ApiResult.ok(b);
    }

    /**
     * 给角色赋予权限
     *
     * @param updateDTO {@link SysRoleMenuVO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/grantPermissionsByRoleId")
    public ApiResult<Boolean> grantPermissionsByRoleId(@Validated @Body List<SysRoleMenuVO> updateDTO) throws BusinessException {
        boolean b = sysRoleService.grantPermissionsByRoleId(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 分页查询角色
     *
     * @param pageDTO {@link SysRolePageDTO}
     * @return {@link ApiPage<SysRoleVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/listByPage")
    public ApiResult<ApiPage<SysRoleVO>> listByPage(@Validated @Body SysRolePageDTO pageDTO) throws BusinessException {
        ApiPage<SysRoleVO> p = sysRoleService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
