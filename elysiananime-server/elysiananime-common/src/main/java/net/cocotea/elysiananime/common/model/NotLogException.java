package net.cocotea.elysiananime.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cocotea.elysiananime.common.enums.ApiResultEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 该异常信息不保存错误日志到数据库
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NotLogException extends Exception implements Serializable {
    @Serial
    private static final long serialVersionUID = 3843777456195536990L;

    /**
     * 异常编号
     */
    private Integer errorCode;

    /**
     * 异常信息
     */
    private String errorMsg;

    public NotLogException(String errorMsg) {
        this.errorMsg = errorMsg;
        this.errorCode = ApiResultEnum.ERROR.getCode();
    }
}