package net.cocotea.elysiananime.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
* 文件信息表
*
* @author CoCoTea 572315466@qq.com
* @version 2.0.0
*/
@Data
@Accessors(chain = true)
public class FileInfo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 *文件名称
	 */
	private String fileName;

	/**
	 *文件后缀
	 */
	private String fileSuffix;

	/**
	 *文件真实路径
	 */
	private String realPath;

	/**
	 *文件大小（单位：字节）
	 */
	private Long fileSize;

	/**
	 * 文件目录
	 */
	private String fileDir;

	/**
	 * 文件基础路径
	 */
	private String fileBasePath;

}