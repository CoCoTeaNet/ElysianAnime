package net.cocotea.janime.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cocotea.janime.common.enums.ApiResultEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 业务异常
 *
 * @author CoCoTea
 * @version v2.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends Exception implements Serializable {
    @Serial
    private static final long serialVersionUID = 4843777456195536990L;

    /**
     * 异常编号
     */
    private Integer errorCode;

    /**
     * 异常信息
     */
    private String errorMsg;

    public BusinessException(String errorMsg) {
        this.errorMsg = errorMsg;
        this.errorCode = ApiResultEnum.ERROR.getCode();
    }
}