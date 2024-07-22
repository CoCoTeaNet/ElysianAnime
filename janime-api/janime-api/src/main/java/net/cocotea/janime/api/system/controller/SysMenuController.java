package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.janime.api.system.model.dto.SysMenuAddDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuPageDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysMenuVO;
import net.cocotea.janime.api.system.service.SysMenuService;
import net.cocotea.janime.common.annotation.LogPersistence;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;

/**
 * @date 2022-1-16 15:37:26
 * @author jwss
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {
    @Resource
    private SysMenuService sysMenuService;

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("add")
    public ApiResult<?> add(@Valid @RequestBody SysMenuAddDTO param) throws BusinessException {
        boolean b = sysMenuService.add(param);
        return ApiResult.flag(b);
    }

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("deleteBatch")
    public ApiResult<?> deleteBatch(@Valid @RequestBody List<BigInteger> idList) throws BusinessException {
        boolean b = sysMenuService.deleteBatch(idList);
        return ApiResult.flag(b);
    }

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("update")
    public ApiResult<?> update(@Valid @RequestBody SysMenuUpdateDTO param) throws BusinessException {
        boolean b = sysMenuService.update(param);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("listByPage")
    public ApiResult<?> listByPage(@Valid @RequestBody SysMenuPageDTO pageParam) throws BusinessException {
        ApiPage<SysMenuVO> menus = sysMenuService.listByPage(pageParam);
        return ApiResult.ok(menus);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("listByTree")
    public ApiResult<?> listByTree(@Valid @RequestBody SysMenuPageDTO param) {
        List<SysMenuVO> menus = sysMenuService.listByTree(param);
        return ApiResult.ok(menus);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("listByTreeAsRoleSelection")
    public ApiResult<?> listByTreeAsRoleSelection(@Valid @RequestBody SysMenuPageDTO pageParam) {
        List<SysMenuVO> menus = sysMenuService.listByTreeAsRoleSelection(pageParam);
        return ApiResult.ok(menus);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @GetMapping("listByRoleId/{roleId}")
    public ApiResult<?> listByRoleId(@PathVariable String roleId) {
        List<SysMenuVO> menus = sysMenuService.listByRoleId(roleId);
        return ApiResult.ok(menus);
    }
}
