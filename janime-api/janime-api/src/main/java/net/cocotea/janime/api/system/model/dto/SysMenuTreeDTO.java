package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SysMenuTreeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 9045158070191611732L;

    /**
     * 是否菜单
     * {@link net.cocotea.janime.common.enums.IsEnum}
     */
    private Integer isMenu;

    /**
     * 权限编号
     */
    private String permissionCode;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单状态
     * {@link net.cocotea.janime.common.enums.MenuStatusEnum}
     */
    private Integer menuStatus;

}
