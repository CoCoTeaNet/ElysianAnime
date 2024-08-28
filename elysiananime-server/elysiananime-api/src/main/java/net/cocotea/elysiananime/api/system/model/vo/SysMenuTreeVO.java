package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@Data
@Accessors(chain = true)
public class SysMenuTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7261224106151023072L;

    private BigInteger id;
    private BigInteger parentId;
    private String menuName;
    private String routerPath;
    private Integer isExternalLink;
    private Integer menuType;
    private Integer sort;
    private String iconPath;
    private Integer menuStatus;

    private List<SysMenuTreeVO> children;
}
