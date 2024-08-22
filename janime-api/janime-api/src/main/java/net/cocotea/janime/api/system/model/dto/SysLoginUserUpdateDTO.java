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

    /**
     * 用户ID
     */
    private String id;

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
    private Integer sex;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户头像地址
     */
    private String avatar;

}
