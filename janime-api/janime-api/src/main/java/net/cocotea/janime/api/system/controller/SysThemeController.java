package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import net.cocotea.janime.api.system.model.dto.SysThemeUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysThemeVO;
import net.cocotea.janime.api.system.service.SysThemeService;
import net.cocotea.janime.common.model.ApiResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Validated
@RequestMapping("/system/theme")
@RestController
public class SysThemeController {
    @Resource
    private SysThemeService sysThemeService;

    @SaCheckLogin
    @PostMapping("/updateByUser")
    public ApiResult<?> updateByUser(@RequestBody SysThemeUpdateDTO param) {
        boolean b = sysThemeService.updateByUser(param);
        return ApiResult.ok(b);
    }

    @SaCheckLogin
    @GetMapping("/loadByUser")
    public ApiResult<?> loadByUser() {
        SysThemeVO b = sysThemeService.loadByUser();
        return ApiResult.ok(b);
    }
}
