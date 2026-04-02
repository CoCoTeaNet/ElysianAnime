package net.cocotea.elysiananime.api.system.controller;

import net.cocotea.elysiananime.api.system.model.vo.SysOverviewVO;
import net.cocotea.elysiananime.api.system.service.SysDashboardService;
import net.cocotea.elysiananime.common.model.ApiResult;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.MethodType;

import java.util.List;

/**
 * 系统仪表盘接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Controller
@Mapping("/system/dashboard")
public class SysDashboardController {
    @Inject
    private SysDashboardService sysDashboardService;

    /**
     * 获取系统数据概览
     *
     * @return {@link SysOverviewVO}
     */
    @Mapping(value = "getCount", method = MethodType.GET)
    public ApiResult<List<SysOverviewVO>> getCount() {
        List<SysOverviewVO> voList = sysDashboardService.getCount();
        return ApiResult.ok(voList);
    }

}
