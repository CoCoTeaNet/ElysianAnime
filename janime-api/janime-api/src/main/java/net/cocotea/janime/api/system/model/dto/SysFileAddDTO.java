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

    private BigInteger id;
    private String fileName;
    private String fileSuffix;
    private String realPath;
    private BigInteger fileSize;

}