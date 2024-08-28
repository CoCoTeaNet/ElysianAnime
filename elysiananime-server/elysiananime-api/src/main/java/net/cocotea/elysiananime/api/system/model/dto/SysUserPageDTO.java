package net.cocotea.elysiananime.api.system.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.common.model.ApiPageDTO;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author CoCoTea
 * @project sss-rbac-admin
 * @version 1.0.0
 * @description sys_menu,系统菜单表  
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysUserPageDTO extends ApiPageDTO implements Serializable{

	@Serial
	private static final long serialVersionUID = 5565588370362046172L;

	@NotNull(message = "查询参数为空")
	private Query sysUser;

	@Data
	public static class Query {

		/**
		 * 用户昵称
		 */
		private String nickname;

		/**
		 * 用户账号
		 */
		private String username;

		/**
		 * 用户性别：{@link net.cocotea.elysiananime.common.enums.SexEnum}
		 */
		private Integer sex;

		/**
		 * 用户手机号
		 */
		private String mobilePhone;

		/**
		 * 用户邮箱
		 */
		private String email;

		/**
		 * 账号状态：{@link net.cocotea.elysiananime.common.enums.AccountStatusEnum}
		 */
		private Integer accountStatus;

	}

}
