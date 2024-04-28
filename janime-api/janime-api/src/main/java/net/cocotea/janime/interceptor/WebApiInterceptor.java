package net.cocotea.janime.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.janime.api.system.service.SysLogService;
import net.cocotea.janime.common.constant.CharConst;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.enums.LogTypeEnum;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.service.RedisService;
import net.cocotea.janime.common.util.IpUtils;
import net.cocotea.janime.properties.DefaultProp;
import org.sagacity.sqltoy.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class WebApiInterceptor implements HandlerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(WebApiInterceptor.class);

    @Resource
    private RedisService redisService;

    @Resource
    private DefaultProp defaultProp;

    @Resource
    private SysLogService sysLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURL = request.getRequestURL().toString();
        // 是否静态路径
        boolean staticApi = isStaticApi(requestURL);
        // 打印请求内容
        logger.info(
                "### 请求IP：{}，请求地址：{}，请求方式，{}，请求方法：{}，是否静态地址：{}",
                request.getRemoteAddr(),
                requestURL,
                request.getMethod(),
                request.getRequestURI(),
                staticApi
        );
        if (staticApi) {
            return true;
        }
        // 接口访问限制
        boolean limitFlag = apiLimitAccessTimes();
        if (limitFlag) {
            throw new BusinessException("操作过快，请稍后再试！");
        }
        // 在线用户续期
        if (StpUtil.isLogin()) {
            if (StringUtil.isBlank(redisService.get(RedisKeyConst.ONLINE_USER))) {
                String loginId = String.valueOf(StpUtil.getLoginId());
                redisService.save(String.format(RedisKeyConst.ONLINE_USER, loginId), loginId, 30L);
            }
        }
        // 保存登录日志与操作日志,如果没有登录不去保存
        if (StpUtil.isLogin()) {
            sysLogService.saveByLogType(LogTypeEnum.OPERATION.getCode(), request);
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 接口访问限制：1秒内运行访问N次
     */
    private boolean apiLimitAccessTimes() {
        boolean flag = false;
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && StpUtil.isLogin()) {
            String ip = IpUtils.getIp(requestAttributes.getRequest());
            String redisKey = ip + CharConst.COLON + StpUtil.getLoginId();
            String value = redisService.get(redisKey);
            if (StrUtil.isBlank(value)) {
                redisService.save(redisKey, String.valueOf(1), 1L);
            } else if (Integer.parseInt(value) <= defaultProp.getOnceVisits()) {
                int count = Integer.parseInt(value);
                count++;
                redisService.save(redisKey, String.valueOf(count), 1L);
            } else {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 是否静态地址，比如/doc.html
     *
     * @param url 地址
     * @return 是返回TRUE
     */
    private boolean isStaticApi(String url) {
        String regex = "^https?://[^/]+(:\\d+)?(/[^?#]*\\.)(html|css|js|jpg|jpeg|gif|png|svg|woff|woff2|ttf|eot)$";
        return ReUtil.isMatch(regex, url);
    }

}
