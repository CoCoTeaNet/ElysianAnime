package net.cocotea.janime.filter;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.janime.api.system.model.dto.SysLogAddDTO;
import net.cocotea.janime.api.system.service.SysLogService;
import net.cocotea.janime.common.annotation.LogPersistence;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.enums.ApiResultEnum;
import net.cocotea.janime.common.enums.LogStatusEnum;
import net.cocotea.janime.common.enums.LogTypeEnum;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.model.NotLogException;
import net.cocotea.janime.common.service.RedisService;
import net.cocotea.janime.properties.DefaultProp;
import net.cocotea.janime.util.LoginUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.*;
import org.sagacity.sqltoy.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

@Component
public class AppFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AppFilter.class);

    @Inject("${sra-admin.once-visits}")
    private Integer visits;

    @Inject
    private DefaultProp defaultProp;

    @Inject
    private SysLogService sysLogService;

    @Inject
    private RedisService redisService;

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        //1.开始计时（用于计算响应时长）
        long start = System.currentTimeMillis();
        try {
            apiLimitAccessTimes(ctx.realIp());

            chain.doFilter(ctx);

            //2.未处理设为404状态
            if (!ctx.getHandled()) {
                ctx.status(404);
            }

            if (ctx.status() == 404) {
                ApiResult<Object> result = new ApiResult<>(ApiResultEnum.NOT_FOUNT.getCode(), null, ApiResultEnum.NOT_FOUNT.getDesc());
                ctx.render(result);
            }

            onlineUsersRenewal();
        } catch (Exception e) {
            ctx.status(ApiResultEnum.SUCCESS.getCode());
            //4.异常捕促与控制
            logger.error(e.getMessage());

            ApiResult<?> result;

            if (e instanceof NotLoginException) {
                logger.error("登录失效异常:{}", e.getMessage());
                result = ApiResult.error(ApiResultEnum.NOT_LOGIN.getCode(), ApiResultEnum.NOT_LOGIN.getDesc());
            } else if (e instanceof NotPermissionException) {
                logger.error("权限不足异常:{}", e.getMessage());
                result = ApiResult.error(ApiResultEnum.NOT_PERMISSION.getCode(), ApiResultEnum.NOT_PERMISSION.getDesc());
            } else if (e instanceof BusinessException) {
                String errorMsg = ((BusinessException) e).getErrorMsg();
                logger.error("业务逻辑异常: {}", errorMsg);
                errorMsg = StrUtil.isBlank(errorMsg) ? ApiResultEnum.ERROR.getDesc() : errorMsg;
                result = ApiResult.error(ApiResultEnum.ERROR.getCode(), errorMsg);
            } else if (e instanceof NotLogException) {
                result = ApiResult.error(ApiResultEnum.ERROR.getCode(), ApiResultEnum.ERROR.getDesc());
            } else if (e instanceof NotRoleException) {
                logger.error("角色未知异常: {}", e.getMessage());
                result = ApiResult.error(ApiResultEnum.NOT_PERMISSION.getCode(), ApiResultEnum.NOT_PERMISSION.getDesc());
            } else {
                logger.error("未知异常: {}", e.getMessage(), e);
                result = ApiResult.error(ApiResultEnum.ERROR.getDesc());
            }

            saveSystemLog(ctx, LogStatusEnum.ERROR.getCode());
            ctx.render(result);
        }
        //5.获得接口响应时长
        long times = System.currentTimeMillis() - start;
        logger.info("用时：{}ms, once-visits: {}", times, visits);
        saveSystemLog(ctx, LogStatusEnum.SUCCESS.getCode());
    }

    /**
     * 保存用户请求日志
     */
    private void saveSystemLog(Context ctx, int logStatus) {
        logger.info("saveLog >>>>> 请求IP：{},请求地址：{},请求方式：{}", ctx.realIp(), ctx.path(), ctx.method());

        if (!defaultProp.getSaveLog()) {
            logger.warn("saveSystemLog >>>>> saveLog is not enable, sra-admin.save-log: false");
            return;
        }

        Handler mainHandler = ctx.mainHandler();
        if (!(mainHandler instanceof Action action)) {
            logger.warn("saveSystemLog >>>>> is not mainHandler");
            return;
        }
        LogPersistence logPersistence = action.method().getAnnotation(LogPersistence.class);
        if (logPersistence == null) {
            logger.debug("saveSystemLog >>>>> LogPersistence is null");
            return;
        }
        if (logPersistence.logType() != LogTypeEnum.OPERATION.getCode()) {
            logger.warn("saveSystemLog >>>>> is not LogTypeEnum.OPERATION");
            return;
        }
        BigInteger loginId = LoginUtils.loginId();
        if (loginId == null) {
            logger.warn("saveSystemLog >>>>> is not login");
            return;
        }
        SysLogAddDTO sysLogAddDTO = new SysLogAddDTO()
                .setIpAddress(ctx.realIp())
                .setRequestWay(ctx.method())
                .setApiPath(ctx.path())
                .setLogType(logPersistence.logType())
                .setOperator(loginId)
                .setLogStatus(logStatus);
        ThreadUtil.execAsync(() -> sysLogService.add(sysLogAddDTO));
    }

    /**
     * 在线用户续期
     */
    private void onlineUsersRenewal() {
        if (StpUtil.isLogin()) {
            String loginId = String.valueOf(StpUtil.getLoginId());
            ThreadUtil.execAsync(() -> {
                String key = String.format(RedisKeyConst.ONLINE_USER, loginId);
                redisService.save(key, loginId, 30L);
            });
        }
    }

    /**
     * 接口访问限制：1秒内运行访问N次
     */
    private void apiLimitAccessTimes(String ip) throws BusinessException {
        if (visits <= 0) {
            return;
        }
        if (StpUtil.isLogin()) {
            String redisKey = ip + ":" + StpUtil.getLoginId();
            String value = redisService.get(redisKey);
            if (StrUtil.isBlank(value)) {
                redisService.save(redisKey, String.valueOf(1), 1L);
                return;
            }
            if (Integer.parseInt(value) <= visits) {
                int count = Integer.parseInt(value);
                count++;
                redisService.set(redisKey, String.valueOf(count));
            } else {
                throw new BusinessException("操作过于频繁");
            }
        }
    }


}
