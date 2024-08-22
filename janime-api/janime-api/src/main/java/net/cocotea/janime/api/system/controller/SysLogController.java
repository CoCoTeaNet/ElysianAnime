package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.janime.api.system.model.dto.SysLogPageDTO;
import net.cocotea.janime.api.system.model.vo.SysLogVO;
import net.cocotea.janime.api.system.service.SysLogService;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统用户访问日志接口
 *
 * @author CoCoTea
 * @version v2.0.0
 */
@Controller
@Mapping("/system/log")
public class SysLogController {
    @Inject
    private SysLogService sysLogService;

    /**
     * 列表分页查询
     *
     * @param sysLogPageDTO {@link SysLogPageDTO}
     * @return {@link ApiPage<SysLogVO>}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("/listByPage")
    public ApiResult<ApiPage<SysLogVO>> pageApiResult(@Validated @Body SysLogPageDTO sysLogPageDTO) throws BusinessException {
        ApiPage<SysLogVO> p = sysLogService.listByPage(sysLogPageDTO);
        return ApiResult.ok(p);
    }

    /**
     * 批量删除
     *
     * @param ids 日志ID集合
     * @return 成功返回TRUE
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post @Mapping("/deleteBatch")
    public ApiResult<Boolean> deleteBatch(@Body List<BigInteger> ids) throws BusinessException {
        boolean b = sysLogService.deleteBatch(ids);
        return ApiResult.ok(b);
    }
}
