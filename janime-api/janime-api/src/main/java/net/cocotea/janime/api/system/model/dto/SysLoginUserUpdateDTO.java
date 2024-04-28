package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 登录用户修改个人信息参数
 * @author CoCoTea
 * @since 2023-4-23 21:15:39
 */
@Data
@Accessors(chain = true)
public class SysLoginUserUpdateDTO {

    private String id;
    private String nickname;
    private String mobilePhone;
    private Integer sex;
    private String email;
    private String avatar;

}
