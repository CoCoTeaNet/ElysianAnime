package net.cocotea.janime.api.system.controller;

import net.cocotea.janime.api.system.model.vo.SysOverviewVO;
import net.cocotea.janime.api.system.model.vo.SystemInfoVO;
import net.cocotea.janime.api.system.service.SysDashboardService;
import net.cocotea.janime.common.model.ApiResult;
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
     * 测试服务是否运行成功
     *
     * @return 字符串
     */
    @Mapping(value = "index", method = MethodType.GET)
    public ApiResult<String> index() {
        return ApiResult.ok("Hello SRA-ADMIN.");
    }

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

    /**
     * 获取服务器运行信息
     *
     * @return {@link SystemInfoVO}
     */
    @Mapping(value = "getSystemInfo", method = MethodType.GET)
    public ApiResult<SystemInfoVO> getSystemInfo() {
        SystemInfoVO vo = sysDashboardService.getSystemInfo();
        return ApiResult.ok(vo);
    }

}
