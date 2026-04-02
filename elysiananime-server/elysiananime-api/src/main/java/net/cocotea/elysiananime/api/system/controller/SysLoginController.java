package net.cocotea.elysiananime.api.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import net.cocotea.elysiananime.api.system.model.dto.SysLoginDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysLoginUserVO;
import net.cocotea.elysiananime.api.system.service.SysLogService;
import net.cocotea.elysiananime.api.system.service.SysUserService;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.enums.LogTypeEnum;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.service.RedisService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统登录相关接口
 *
 * @author CoCoTea
 */
@Controller
@Mapping("/system")
public class SysLoginController {

    private static final Logger log = LoggerFactory.getLogger(SysLoginController.class);

    @Inject
    private SysUserService userService;

    @Inject
    private RedisService redisService;

    @Inject
    private SysLogService sysLogService;

    /**
     * 后台系统用户登录
     *
     * @param loginDTO {@link SysLoginDTO}
     * @param context  {@link Context}
     * @return token
     */
    @Post
    @Mapping("/login")
    public ApiResult<String> login(@Validated @Body SysLoginDTO loginDTO, Context context) throws BusinessException {
        // 开始登录逻辑
        String token = userService.login(loginDTO, context);
        // 保存登录日志
        sysLogService.saveByLogType(LogTypeEnum.LOGIN.getCode(), context);
        return ApiResult.ok(token);
    }

    /**
     * 后台系统用户退出登录
     *
     * @return {@link ApiResult}
     */
    @Post
    @Mapping("/logout")
    public ApiResult<?> logout() {
        // 删除权限缓存
        redisService.delete(String.format(RedisKeyConst.USER_PERMISSION, StpUtil.getLoginId()));
        redisService.delete(String.format(RedisKeyConst.ONLINE_USER, StpUtil.getLoginId()));
        StpUtil.logout();
        return ApiResult.ok();
    }

    /**
     * 获取用户登录信息
     *
     * @return {@link SysLoginUserVO}
     */
    @Get
    @Mapping("/loginInfo")
    public ApiResult<SysLoginUserVO> loginInfo() {
        SysLoginUserVO r = userService.loginUser();
        return ApiResult.ok(r);
    }

}
