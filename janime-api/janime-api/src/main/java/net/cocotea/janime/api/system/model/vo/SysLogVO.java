package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SysLogVO {

    private BigInteger id;
    private String ipAddress;
    private String operator;
    private String username;
    private String nickname;
    private String requestWay;
    private Integer logStatus;
    private Integer logType;
    private LocalDateTime createTime;

}
