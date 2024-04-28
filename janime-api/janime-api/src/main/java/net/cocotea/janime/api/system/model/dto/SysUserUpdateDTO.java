package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
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

    @NotNull(message = "用户ID为空")
    private BigInteger id;
    @NotBlank(message = "账号名为空")
    private String username;
    private String nickname;
    private String mobilePhone;
    private String sex;
    private String email;
    private String avatar;
    private String password;
    private String accountStatus;

    @NotEmpty(message = "角色ID不能为空")
    private List<BigInteger> roleIds;

}
