package net.cocotea.janime.api.system.model.dto;


import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotEmpty;
import org.noear.solon.validation.annotation.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统用户更新传输对象
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysUserUpdateDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID为空")
    private BigInteger id;

    /**
     * 账号名
     */
    @NotBlank(message = "账号名为空")
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户手机号
     */
    private String mobilePhone;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 账号状态
     */
    private String accountStatus;

    /**
     * 角色ID
     */
    @NotEmpty(message = "角色ID不能为空")
    private List<BigInteger> roleIds;

}
