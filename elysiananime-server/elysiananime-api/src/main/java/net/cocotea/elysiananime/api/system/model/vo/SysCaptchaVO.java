package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 验证码视图对象
 *
 * @author CoCoTea
 * @version 1.0.3
 */
@Data
@Accessors(chain = true)
public class SysCaptchaVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8446236240287643428L;

    /**
     * 公钥
     */
    private String publicKey;

}
