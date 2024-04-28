package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysUserVO {

    private BigInteger id;
    private String username;
    private String nickname;
    private Integer sex;
    private String email;
    private String mobilePhone;
    private Integer accountStatus;
    private String avatar;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private BigInteger createBy;
    private LocalDateTime createTime;
    private BigInteger updateBy;
    private LocalDateTime updateTime;

    /**
     * 角色列表
     */
    private List<SysRoleVO> roleList;
}
