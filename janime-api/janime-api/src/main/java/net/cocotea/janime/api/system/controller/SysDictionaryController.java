package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.janime.api.system.model.dto.SysDictionaryAddDTO;
import net.cocotea.janime.api.system.model.dto.SysDictionaryPageDTO;
import net.cocotea.janime.api.system.model.dto.SysDictionaryUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysDictionaryVO;
import net.cocotea.janime.api.system.service.SysDictionaryService;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/**
 * @author jwss
 * @date 2022-3-22
 */
@Validated
@RestController
@RequestMapping("/system/dictionary")
public class SysDictionaryController {
    @Resource
    private SysDictionaryService sysDictionaryService;

    @PostMapping("/add")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<?> add(@Valid @RequestBody SysDictionaryAddDTO param) throws BusinessException {
        boolean b = sysDictionaryService.add(param);
        return ApiResult.flag(b);
    }

    @PostMapping("/deleteBatch")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<?> deleteBatch(@Valid @RequestBody List<BigInteger> list) throws BusinessException {
        boolean b = sysDictionaryService.deleteBatch(list);
        return ApiResult.flag(b);
    }

    @PostMapping("/update")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<?> update(@Valid @RequestBody SysDictionaryUpdateDTO param) throws BusinessException {
        boolean b = sysDictionaryService.update(param);
        return ApiResult.flag(b);
    }

    @PostMapping("/listByTree")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<?> listByTree(@Valid @RequestBody SysDictionaryPageDTO dto) {
        List<SysDictionaryVO> list = sysDictionaryService.listByTree(dto);
        return ApiResult.ok(list);
    }
}
