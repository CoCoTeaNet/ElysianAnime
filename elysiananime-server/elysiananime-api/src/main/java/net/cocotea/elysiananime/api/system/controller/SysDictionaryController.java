package net.cocotea.elysiananime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.elysiananime.api.system.model.dto.SysDictionaryAddDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysDictionaryPageDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysDictionaryTreeDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysDictionaryUpdateDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysDictionaryVO;
import net.cocotea.elysiananime.api.system.service.SysDictionaryService;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统字典管理接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Controller
@Mapping("/system/dictionary")
@Valid
public class SysDictionaryController {
    @Inject
    private SysDictionaryService sysDictionaryService;

    /**
     * 新增字典
     *
     * @param dictionaryAddDTO {@link SysDictionaryAddDTO}
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body SysDictionaryAddDTO dictionaryAddDTO) throws BusinessException {
        boolean b = sysDictionaryService.add(dictionaryAddDTO);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除
     *
     * @param list 字典主键ID集合
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) throws BusinessException {
        boolean b = sysDictionaryService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    /**
     * 更新字典信息
     *
     * @param param {@link SysDictionaryUpdateDTO}
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body SysDictionaryUpdateDTO param) throws BusinessException {
        boolean b = sysDictionaryService.update(param);
        return ApiResult.ok(b);
    }

    /**
     * 分页获取字典树形列表
     *
     * @param dictionaryPageDTO {@link SysDictionaryPageDTO}
     * @return {@link SysDictionaryVO}
     */
    @Mapping("/listByTree")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<List<SysDictionaryVO>> listByTree(@Validated @Body SysDictionaryTreeDTO dictionaryPageDTO) {
        List<SysDictionaryVO> list = sysDictionaryService.listByTree(dictionaryPageDTO);
        return ApiResult.ok(list);
    }
}
