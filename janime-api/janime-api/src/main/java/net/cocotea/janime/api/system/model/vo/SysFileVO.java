package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
* 系统文件表
*
* @author CoCoTea 572315466@qq.com
* @version v2.0.0
*/
@Data
@Accessors(chain = true)
public class SysFileVO implements Serializable {

	/**
	 *主键ID
	 */
	private String id;

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
	 *浏览路径
	 */
	private String browsePath;

	/**
	 *文件访问地址
	 */
	private String fullPath;

	/**
	 *文件大小（单位：字节）
	 */
	private Long fileSize;

	/**
	 *创建人
	 */
	private String createBy;

	/**
	 *创建时间
	 */
	private Date createTime;

	/**
	 *更新人
	 */
	private String updateBy;

	/**
	 *更新时间
	 */
	private Date updateTime;

	/**
	 * 是否共享
	 */
	private Integer isShare;

	private LocalDateTime beginTime;
	private LocalDateTime endTime;

}