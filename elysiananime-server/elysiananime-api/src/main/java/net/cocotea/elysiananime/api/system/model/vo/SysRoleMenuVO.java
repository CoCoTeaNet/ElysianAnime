package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author CoCoTea
 * @project sss-rbac-admin
 * @version 1.0.0
 * @description sys_role_menu,角色菜单关联表  
 */
@Data
@Accessors(chain = true)
public class SysRoleMenuVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 5632526424183418214L;
	
	/**
	 * 角色主键ID
	 */
	private String roleId;
	
	/**
	 * 菜单主键ID
	 */
	private String menuId;
	
}
