package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jwss
 * @version 1.0.0
 * @description sys_dictionary,系统字典表  
 */
@Data
@Accessors(chain = true)
public class SysDictionaryUpdateDTO implements Serializable {
	private static final long serialVersionUID = -1767974905556894292L;

	@NotBlank(message = "主键ID为空")
	private String id;

	private String parentId;
	
	private String dictionaryName;
	
	private String remark;
	
	private Integer sort;
	
	private String enableStatus;
}
