package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.janime.api.system.model.dto.SysVersionAddDTO;
import net.cocotea.janime.api.system.model.dto.SysVersionPageDTO;
import net.cocotea.janime.api.system.model.dto.SysVersionUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysVersionVO;
import net.cocotea.janime.api.system.service.SysVersionService;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;

@RequestMapping("/system/version")
@RestController
public class SysVersionController {
    @Resource
    private SysVersionService sysVersionService;
    
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/add")
    public ApiResult<?> add(@Valid @RequestBody SysVersionAddDTO param) throws BusinessException {
        boolean b = sysVersionService.add(param);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/deleteBatch")
    public ApiResult<?> deleteBatch(@RequestBody List<BigInteger> param) throws BusinessException {
        boolean b = sysVersionService.deleteBatch(param);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public ApiResult<?> update(@Valid @RequestBody SysVersionUpdateDTO param) throws BusinessException {
        boolean b = sysVersionService.update(param);
        return ApiResult.flag(b);
    }

    @PostMapping("/listByPage")
    public ApiResult<?> listByPage(@Valid @RequestBody SysVersionPageDTO pageDTO) throws BusinessException {
        ApiPage<SysVersionVO> r = sysVersionService.listByPage(pageDTO);
        return ApiResult.ok(r);
    }
    
}
