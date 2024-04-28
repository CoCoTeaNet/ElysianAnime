package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import net.cocotea.janime.api.system.model.dto.SysCaptchaDTO;
import net.cocotea.janime.api.system.model.dto.SysLoginDTO;
import net.cocotea.janime.api.system.model.vo.SysLoginUserVO;
import net.cocotea.janime.api.system.service.SysUserService;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.service.RedisService;
import net.cocotea.janime.common.util.IpUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author CoCoTea
 */
@Validated
@RestController
@RequestMapping("/system")
public class SysLoginController {
    @Resource
    private SysUserService userService;

    @Resource
    private RedisService redisService;

    @PostMapping("/login")
    public ApiResult<?> login(@Valid @RequestBody SysLoginDTO dto, HttpServletRequest request) throws BusinessException {
        userService.login(dto, request);
        return ApiResult.ok();
    }

    @PostMapping("/logout")
    public ApiResult<?> logout() {
        // 删除权限缓存
        redisService.delete(String.format(RedisKeyConst.USER_PERMISSION, StpUtil.getLoginId()));
        redisService.delete(String.format(RedisKeyConst.ONLINE_USER, StpUtil.getLoginId()));
        StpUtil.logout();
        return ApiResult.ok();
    }

    @GetMapping("/loginInfo")
    public ApiResult<?> loginInfo() {
        SysLoginUserVO r = userService.loginUser();
        return ApiResult.ok(r);
    }

    @PostMapping("/captcha")
    public ApiResult<String> captcha(@Valid @RequestBody SysCaptchaDTO captchaDTO, HttpServletRequest request) {
        // 生成圆圈干扰的验证码
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200, 100, 4, 20);
        redisService.save(
                String.format(RedisKeyConst.VERIFY_CODE, captchaDTO.getCodeType(), IpUtils.getIp(request)),
                captcha.getCode(),
                300L
        );
        return ApiResult.ok(captcha.getImageBase64());
    }
}
