package net.cocotea.elysiananime.api.system.model.dto;

import org.noear.solon.validation.annotation.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
* 系统文件信息修改参数
*
* @author CoCoTea 572315466@qq.com
* @version 2.0.5
*/
@Data
@Accessors(chain = true)
public class SysFileUpdateDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 文件ID
	 */
	@NotNull(message = "文件ID不能为空")
	private String id;

	/**
	 *文件名称
	 */
	private String fileName;

	/**
	 * 是否共享
	 */
	private Integer isShare;

}