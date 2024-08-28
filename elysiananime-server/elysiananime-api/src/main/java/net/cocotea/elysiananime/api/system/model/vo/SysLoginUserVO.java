package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * @author CoCoTea
 * @version 1.0.0
 * @project sss-rbac-admin
 * @description sys_user, 系统用户表
 */
@Data
@Accessors(chain = true)
public class SysLoginUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1641777558644610990L;

    private Boolean loginStatus;

    private BigInteger id;

    private String username;

    private String nickname;

    private String avatar;

    private List<SysMenuTreeVO> menuList;

    private String token;

}
