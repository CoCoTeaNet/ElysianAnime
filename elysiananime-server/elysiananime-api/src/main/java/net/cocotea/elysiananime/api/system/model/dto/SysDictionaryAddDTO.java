package net.cocotea.elysiananime.api.system.model.dto;


import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysDictionaryAddDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = -1767974905556894292L;

	/**
	 * 父节点主键ID
	 */
	private BigInteger parentId;

	/**
	 * 字典名称
	 */
	@NotBlank(message = "字典名称不能为空")
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
	private Integer enableStatus;
}
