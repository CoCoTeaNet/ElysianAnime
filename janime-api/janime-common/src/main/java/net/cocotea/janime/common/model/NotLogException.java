package net.cocotea.janime.common.model;

import net.cocotea.janime.common.enums.ApiResultEnum;

import java.io.Serializable;

/**
 * 该异常信息不保存错误日志到数据库
 *
 * @date 2022-9-13 15:04:58
 * @author CoCoTea
 */
public class NotLogException extends Exception implements Serializable {
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

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}