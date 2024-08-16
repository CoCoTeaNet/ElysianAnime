package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1116543069796275006L;

    /**
     * 用户ID
     */
    private BigInteger id;

    /**
     * 账户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户性别
     */
    private Integer sex;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 用户手机号
     */
    private String mobilePhone;

    /**
     * 账号状态
     */
    private Integer accountStatus;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 最后登录ip
     */
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建人
     */
    private BigInteger createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private BigInteger updateBy;

    /**
     *更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 角色列表
     */
    private List<SysRoleVO> roleList;
}
