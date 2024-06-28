package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 验证码视图对象
 *
 * @author CoCoTea
 * @version 2.0_202406
 */
@Data
@Accessors(chain = true)
public class SysCaptchaVO {

    /**
     * 验证码ID
     */
    private String captchaId;

    /**
     * 验证码base64字符串
     */
    private String imgBase64;

    /**
     * 公钥
     */
    private String publicKey;

}
