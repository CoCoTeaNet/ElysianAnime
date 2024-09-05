package net.cocotea.elysiananime.properties;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * 默认值配置项
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Component("defaultProp")
public class DefaultProp {

    /**
     * 默认密码
     */
    @Inject("${sra-admin.password}")
    private String password;

    /**
     * 密码加密的盐
     */
    @Inject("${sra-admin.password-salt}")
    private String passwordSalt;

    /**
     * 1秒内限制api访问的次数
     */
    @Inject("${sra-admin.once-visits}")
    private Integer onceVisits;

    /**
     * 是否开启权限缓存: true开启，false关闭
     */
    @Inject("${sra-admin.permission-cache}")
    private Boolean permissionCache;

    /**
     * 是否开启系统日志保存功能
     */
    @Inject("${sra-admin.save-log}")
    private Boolean saveLog;

    /**
     * 强密码：启用后会关闭图片验证码验证
     */
    @Inject("${sra-admin.strong-password}")
    private String strongPassword;

    /**
     * 路由放行地址
     */
    @Inject("${sra-admin.excludes}")
    private String excludes;


    /**
     * 前端访问地址
     */
    @Inject("${elysiananime.url.web}")
    private String webUrl;

    /**
     * 观看到某个时长时，自动更新观看状态为'在看' (单位秒)
     */
    @Inject("${elysiananime.auto-reading-time}")
    private Long autoReadingTime;
}
