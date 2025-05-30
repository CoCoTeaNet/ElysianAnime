package net.cocotea.elysiananime.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.solon.dao.SaTokenDaoOfRedisJson;
import cn.dev33.satoken.solon.integration.SaTokenInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONWriter;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.serialization.fastjson2.Fastjson2ActionExecutor;
import org.noear.solon.serialization.fastjson2.Fastjson2RenderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author jwss
 * @date 2022-1-17 16:12:05
 */
@Configuration
public class WebMvcConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Bean(index = -100)  //-100，是顺序位（低值优先）
    public SaTokenInterceptor saTokenInterceptor(@Inject("${sra-admin.excludes}") String excludesStr) {
        SaTokenInterceptor interceptor = new SaTokenInterceptor();

        String[] excludes = excludesStr.split(",");

        // 指定 [拦截路由] 与 [放行路由]
        interceptor.addInclude("/**");
        for (String url : excludes) {
            logger.info("[saTokenInterceptor]放行接口：{}", url);
            interceptor.addExclude(url);
        }

        // 认证函数: 每次请求执行
        interceptor.setAuth(req -> SaRouter.match("/**", StpUtil::checkLogin));

        // 前置函数：在每次认证函数之前执行
        interceptor.setBeforeAuth(req -> {
            // ---------- 设置一些安全响应头 ----------
            SaHolder.getResponse()
                    // 服务器名称
                    .setServer("sa-server")
                    // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
                    .setHeader("X-Frame-Options", "SAMEORIGIN")
                    // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
                    .setHeader("X-XSS-Protection", "1; mode=block")
                    // 禁用浏览器内容嗅探
                    .setHeader("X-Content-Type-Options", "nosniff");
        });

        return interceptor;
    }

    @Bean
    public void jsonInit(@Inject Fastjson2RenderFactory factory, @Inject Fastjson2ActionExecutor executor){
        // 日期转换
        factory.addConvertor(Date.class, s -> DateUtil.format(s, DatePattern.NORM_DATETIME_PATTERN));
        factory.addConvertor(LocalDateTime.class, s -> DateUtil.format(s, DatePattern.NORM_DATETIME_PATTERN));

        factory.addFeatures(
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue,
                JSONWriter.Feature.WriteNullNumberAsZero,
                JSONWriter.Feature.WriteNullStringAsEmpty,
                JSONWriter.Feature.WriteLongAsString,
                JSONWriter.Feature.WriteBigDecimalAsPlain,
                JSONWriter.Feature.BrowserCompatible
        );
    }

    /**
     * 使用Redis缓存token
     */
    @Bean
    public SaTokenDao saTokenDaoInit(@Inject("${myapp.rd1}") SaTokenDaoOfRedisJson saTokenDao) {
        return saTokenDao;
    }

}
