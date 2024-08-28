package net.cocotea.elysiananime.api.system.model.dto;


import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * 系统用户新增对象
 *
 * @author CoCoTea
 * @version 2.0.0
 * @project sss-rbac-admin
 * @description sys_user, 系统用户表
 */
@Data
@Accessors(chain = true)
public class SysUserAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -75070990767806255L;

    /**
     * 用户名称
     */
    @NotBlank(message = "账号名为空")
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 账号状态
     */
    private String accountStatus;

    /**
     * 用户角色
     */
    @NotNull(message = "角色ID为空")
    private List<BigInteger> roleIds;

}
