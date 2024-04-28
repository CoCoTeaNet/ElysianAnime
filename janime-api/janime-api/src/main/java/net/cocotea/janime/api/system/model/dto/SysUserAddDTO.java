package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * @author CoCoTea
 * @project sss-rbac-admin
 * @version 1.2.6
 * @description sys_user,系统用户表  
 */
@Data
@Accessors(chain = true)
public class SysUserAddDTO implements Serializable {

	private static final long serialVersionUID = -75070990767806255L;

	@NotBlank(message = "账号名为空")
	private String username;

	private String nickname;

	private String sex;

	private String email;

	private String password;

	private String accountStatus;

	@NotNull(message = "角色ID为空")
	private List<BigInteger> roleIds;

}
