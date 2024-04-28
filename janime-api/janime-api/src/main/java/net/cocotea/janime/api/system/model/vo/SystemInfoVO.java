package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @date 2022-1-26 11:41:05
 * @author jwss
 */
@Data
@Accessors(chain = true)
public class SystemInfoVO implements Serializable {
    private static final long serialVersionUID = 8671770549214978920L;

    private String os;

    private String serverName;

    private String serverIp;

    private String serverArchitecture;

    private String javaName;

    private String javaVersion;

    private String javaPath;

    private String projectPath;

    private Long runningTime;

    private Double cpuSystemUsed;

    private Double cpuUserUsed;

    private Double cpuFree;

    private Integer cpuCount;

    private Long memoryTotalSize;

    private Long memoryAvailableSize;

    private Long diskTotalSize;

    private Long diskFreeSize;

    private String diskPath;

    private String diskSeparator;

}
