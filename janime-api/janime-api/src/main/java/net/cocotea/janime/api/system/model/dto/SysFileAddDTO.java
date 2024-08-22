package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;

/**
 * 系统文件表
 *
 * @author CoCoTea 572315466@qq.com
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysFileAddDTO {

    /**
     * 文件ID
     */
    private BigInteger id;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 真实路径
     */
    private String realPath;

    /**
     * 文件大小
     */
    private BigInteger fileSize;

}