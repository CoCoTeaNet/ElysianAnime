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

    /**
     * 请求方法
     */
    private String requestWay;

    /**
     * 接口路径
     */
    private String apiPath;

    private Integer logStatus;
    private Integer logType;
    private LocalDateTime createTime;

}
