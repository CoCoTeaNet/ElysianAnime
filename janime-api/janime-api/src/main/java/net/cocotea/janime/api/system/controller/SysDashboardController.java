package net.cocotea.janime.api.system.controller;

import cn.hutool.json.JSONUtil;
import net.cocotea.janime.api.system.model.vo.SystemInfoVO;
import net.cocotea.janime.api.system.service.SysDashboardService;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.service.RedisService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @date 2022-1-26 11:36:32
 * @author jwss
 */
@Validated
@RestController
@RequestMapping("/system/dashboard")
public class SysDashboardController {
    @Resource
    private SysDashboardService sysDashboardService;

    @Resource
    private RedisService redisService;

    @GetMapping("index")
    public ApiResult<String> index() {
        return ApiResult.ok("Hello sss-rbac-admin.");
    }

    @GetMapping("getCount")
    public ApiResult<List<Map<String, Object>>> getCount() {
        List<Map<String, Object>> count = sysDashboardService.getCount();
        return ApiResult.ok(count);
    }

    @GetMapping("getSystemInfo")
    public ApiResult<SystemInfoVO> getSystemInfo() {
        SystemInfoVO vo = sysDashboardService.getSystemInfo();
        return ApiResult.ok(vo);
    }

    @GetMapping("getRssWorkStatus")
    public ApiResult<?> getRssWorkStatus() {
        String message = redisService.get(RedisKeyConst.RSS_WORKS_STATUS);
        return ApiResult.ok(JSONUtil.parse(message));
    }

}
