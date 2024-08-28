package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @date 2022-1-26 11:41:05
 * @author CoCoTea
 */
@Data
@Accessors(chain = true)
public class SystemInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8671770549214978920L;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 服务IP
     */
    private String serverIp;

    /**
     * 服务器架构
     */
    private String serverArchitecture;

    /**
     * JAVA名称
     */
    private String javaName;

    /**
     * JAVA版本
     */
    private String javaVersion;

    /**
     * JAVA存放路径
     */
    private String javaPath;

    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 服务运行时长
     */
    private Long runningTime;

    /**
     * CPU系统使用率
     */
    private Double cpuSystemUsed;

    /**
     * CPU用户使用率
     */
    private Double cpuUserUsed;

    /**
     * CPU可用率
     */
    private Double cpuFree;

    /**
     * CPU数量
     */
    private Integer cpuCount;

    /**
     * 内存总大小
     */
    private Long memoryTotalSize;

    /**
     * 内存可用大小
     */
    private Long memoryAvailableSize;

    /**
     * 磁盘总大小
     */
    private Long diskTotalSize;

    /**
     * 磁盘可用大小
     */
    private Long diskFreeSize;

    /**
     * 盘符路径
     */
    private String diskPath;

    /**
     * 磁盘分隔符
     */
    private String diskSeparator;

}
