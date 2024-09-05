package net.cocotea.elysiananime.api.system.model.dto;


import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author CoCoTea
 * @version 1.0.0
 * @description sys_dictionary,系统字典表  
 */
@Data
@Accessors(chain = true)
public class SysDictionaryUpdateDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = -1767974905556894292L;

	/**
	 * 主键ID
	 */
	@NotBlank(message = "主键ID为空")
	private String id;

	/**
	 * 父节点主键ID
	 */
	private String parentId;

	/**
	 * 字典名称
	 */
	private String dictionaryName;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 序号
	 */
	private Integer sort;

	/**
	 * 启用状态;0关闭 1启用
	 */
	private String enableStatus;
}
