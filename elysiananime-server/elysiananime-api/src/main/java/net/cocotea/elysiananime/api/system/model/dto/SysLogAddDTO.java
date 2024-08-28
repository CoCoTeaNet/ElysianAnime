package net.cocotea.elysiananime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 新增操作日志参数
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysLogAddDTO {

    /**
     * ip地址
     */
    private String ipAddress;

    /**
     * 操作人ID
     */
    private BigInteger operator;

    /**
     * 请求方式
     */
    private String requestWay;

    /**
     * 日志状态
     */
    private Integer logStatus;

    /**
     * 日志类型：{@link net.cocotea.elysiananime.common.enums.LogTypeEnum}
     */
    private Integer logType;

    /**
     *  接口路径
     */
    private String apiPath;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
