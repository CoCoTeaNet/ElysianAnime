package net.cocotea.elysiananime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.elysiananime.api.system.model.dto.SysVersionAddDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysVersionPageDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysVersionUpdateDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysVersionVO;
import net.cocotea.elysiananime.api.system.service.SysVersionService;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统版本管理接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Mapping("/system/version")
@Controller
@Valid
public class SysVersionController {
    @Inject
    private SysVersionService sysVersionService;

    /**
     * 新增版本信息
     *
     * @param addDTO {@link SysVersionAddDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("/add")
    public ApiResult<Boolean> add(@Validated @Body SysVersionAddDTO addDTO) throws BusinessException {
        boolean b = sysVersionService.add(addDTO);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除版本
     *
     * @param idList id集合
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/deleteBatch")
    public ApiResult<Boolean> deleteBatch(@Body List<BigInteger> idList) throws BusinessException {
        boolean b = sysVersionService.deleteBatch(idList);
        return ApiResult.ok(b);
    }

    /**
     * 更新版本信息
     *
     * @param updateDTO {@link SysVersionUpdateDTO}
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/update")
    public ApiResult<Boolean> update(@Validated @Body SysVersionUpdateDTO updateDTO) throws BusinessException {
        boolean b = sysVersionService.update(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 分页查询版本信息
     *
     * @param pageDTO {@link SysVersionPageDTO}
     * @return {@link ApiPage<SysVersionVO>}
     */
    @Post @Mapping("/listByPage")
    public ApiResult<ApiPage<SysVersionVO>> listByPage(@Validated @Body SysVersionPageDTO pageDTO) throws BusinessException {
        ApiPage<SysVersionVO> r = sysVersionService.listByPage(pageDTO);
        return ApiResult.ok(r);
    }

}
