package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.api.system.model.po.SysUserRole;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysUserRoleVO extends SysUserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 4531576278077869409L;

    /**
     * 角色名称
     */
    private String roleName;

}
