package net.cocotea.janime.api.system.model.dto;


import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author CoCoTea
 * @project sss-rbac-admin
 * @version 1.0.0
 * @description sys_role,系统角色表  
 */
@Data
@Accessors(chain = true)
public class SysRoleUpdateDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = -551717904814153800L;

	@NotBlank(message = "主键ID不能为空")
	private String id;

	/**
	 * 角色名称
	 */
	private String roleName;
	
	/**
	 * 角色标识
	 */
	private String roleKey;
	
	/**
	 * 显示排序
	 */
	private Integer sort;

}
