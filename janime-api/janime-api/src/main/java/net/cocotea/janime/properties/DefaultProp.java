package net.cocotea.janime.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认值配置项
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Component
public class DefaultProp {

    /**
     * 前端访问地址
     */
    @Value("${janime.url.web}")
    private String webUrl;

    /**
     * 默认密码
     */
    @Value("${janime.password}")
    private String password;

    /**
     * 密码加密的盐
     */
    @Value("${janime.password-salt}")
    private String passwordSalt;

    /**
     * 1秒内限制api访问的次数
     */
    @Value("${janime.once-visits}")
    private Integer onceVisits;

    /**
     * 是否开启权限缓存: true开启，false关闭
     */
    @Value("${janime.permission-cache}")
    private Boolean permissionCache;

    /**
     * 是否开启系统日志保存功能
     */
    @Value("${janime.save-log}")
    private Boolean saveLog;

    /**
     * 强密码：启用后会关闭图片验证码验证
     */
    @Value("${janime.strong-password}")
    private String strongPassword;

    /**
     * 路由放行地址
     */
    @Value("${janime.excludes}")
    private String excludes;

}
