package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jwss
 * @project sss-rbac-admin
 * @version 1.0.0
 * @description sys_role,系统角色表  
 */
@Data
@Accessors(chain = true)
public class SysRoleAddDTO implements Serializable {

	private static final long serialVersionUID = -6663565327923196565L;

	/**
	 * 角色名称
	 */
	@NotBlank(message = "角色名称为空")
	private String roleName;
	
	/**
	 * 角色标识
	 */
	@NotBlank(message = "角色标识为空")
	private String roleKey;
	
	/**
	 * 显示排序
	 */
	private Integer sort;

}
