package net.cocotea.janime.api.system.model.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 图片验证码参数
 *
 * @author CoCoTea
 */
@Data
@Accessors(chain = true)
public class SysCaptchaDTO implements Serializable {
    private static final long serialVersionUID = 8511400433671973527L;

    /**
     * {@link net.cocotea.janime.common.constant.CommonConst}
     */
    @NotBlank(message = "验证码类型为空")
    private String codeType;
    /**
     * 拓展参数
     */
    private JSONObject extraParam;

}
