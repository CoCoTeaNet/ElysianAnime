package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysDictionaryAddDTO implements Serializable {
	private static final long serialVersionUID = -1767974905556894292L;

	private BigInteger parentId;

	@NotBlank(message = "字典名称不能为空")
	private String dictionaryName;
	
	private String remark;
	
	private Integer sort;
	
	private Integer enableStatus;
}
