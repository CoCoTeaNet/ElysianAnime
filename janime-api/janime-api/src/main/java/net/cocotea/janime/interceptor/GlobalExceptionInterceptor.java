package net.cocotea.janime.interceptor;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import net.cocotea.janime.api.system.service.SysLogService;
import net.cocotea.janime.common.enums.ApiResultEnum;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.model.NotLogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 全局异常
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@RestControllerAdvice
public class GlobalExceptionInterceptor {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionInterceptor.class);

    @Resource
    private SysLogService sysLogService;

    @ExceptionHandler(Exception.class)
    public ApiResult<?> handlerException(Exception ex) {
        logger.error(">>>>>>系统异常:{}", ex.getMessage(), ex);
        saveLog();
        return ApiResult.error("系统异常，请联系管理员~");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<?> handlerHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        logger.error(">>>>>>请求方法异常:{}", ex.getMessage(), ex);
        return ApiResult.error(ApiResultEnum.METHOD_ERROR.getCode(), ApiResultEnum.METHOD_ERROR.getDesc());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error(">>>>>>校验参数异常:{}", e.getMessage());
        saveLog();
        return ApiResult.error(Objects.requireNonNull(e.getFieldError()).getDefaultMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public ApiResult<?> handlerNotLoginException(NotLoginException e) {
        logger.error(">>>>>>登录失效异常:{}", e.getMessage());
        saveLog();
        return ApiResult.error(ApiResultEnum.NOT_LOGIN.getCode(), ApiResultEnum.NOT_LOGIN.getDesc());
    }

    @ExceptionHandler(NotPermissionException.class)
    public ApiResult<?> handlerNotPermissionException(NotPermissionException e) {
        logger.error(">>>>>>权限不足异常:{}", e.getMessage());
        saveLog();
        return ApiResult.error(ApiResultEnum.NOT_PERMISSION.getCode(), ApiResultEnum.NOT_PERMISSION.getDesc());
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResult<?> handlerBusinessException(BusinessException e) {
        logger.error(">>>>>>业务逻辑异常: {}", e.getErrorMsg());
        saveLog();
        return ApiResult.error(e.getErrorCode(), e.getErrorMsg());
    }

    @ExceptionHandler(NotLogException.class)
    public ApiResult<?> handlerNotLogException(NotLogException e) {
        logger.error(">>>>>>无日志异常: {}", e.getMessage());
        return ApiResult.error(e.getErrorCode(), e.getErrorMsg());
    }

    @ExceptionHandler(NotRoleException.class)
    public ApiResult<?> handlerNotRoleException(NotRoleException e) {
        logger.error(">>>>>>角色未知异常: {}", e.getMessage());
        saveLog();
        return ApiResult.error(ApiResultEnum.NOT_PERMISSION.getCode(), ApiResultEnum.NOT_PERMISSION.getDesc());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<?> handlerMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        logger.error(">>>>> 参数缺失异常: {}", ex.getMessage());
        return ApiResult.error(ApiResultEnum.MISSING_REQUEST_PARAMETER.getCode(), ApiResultEnum.MISSING_REQUEST_PARAMETER.getDesc());
    }

    private void saveLog() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            logger.info("saveLog >>>>> 异常日志打印，请求IP：{},请求地址：{},请求方式：{}", request.getRemoteAddr(), request.getRequestURL().toString(), request.getMethod());
            // 保存登录日志与操作日志,如果没有登录不去保存
            sysLogService.saveErrorLog(request);
        } else {
            logger.error("saveLog >>>>> ServletRequestAttributes is null");
        }
    }
}
