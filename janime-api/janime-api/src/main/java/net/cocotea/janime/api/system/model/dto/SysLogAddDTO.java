package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Id;

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

    private String ipAddress;
    private BigInteger operator;
    private String requestWay;
    private Integer logStatus;
    private Integer logType;
    private LocalDateTime createTime;

}
