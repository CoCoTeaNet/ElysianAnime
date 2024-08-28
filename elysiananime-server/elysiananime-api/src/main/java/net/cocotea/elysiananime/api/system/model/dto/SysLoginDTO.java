package net.cocotea.elysiananime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统登录接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -75070990767806255L;

    /**
     * 用户账号
     */
    @NotBlank(message = "账号名不能为空")
    private String username;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 登录验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /**
     * 验证码ID
     */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    /**
     * 公钥
     */
    @NotBlank(message = "公钥不能为空")
    private String publicKey;

    /**
     * 是否记住我
     */
    private Boolean rememberMe;

}
