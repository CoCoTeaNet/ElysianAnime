package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SysVersionVO {

    /**
     * 版本ID
     */
    private BigInteger id;

    /**
     * 更新版本号
     */
    private String updateNo;

    /**
     * 版本更新描述
     */
    private String updateDesc;

    /**
     * 系统平台名称
     */
    private String platformName;

    /**
     * 下载地址
     */
    private String downloadUrl;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
