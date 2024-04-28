package net.cocotea.janime.api.system.model.dto;

import cn.hutool.core.convert.Convert;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.api.system.model.po.SysMenu;
import net.cocotea.janime.api.system.model.vo.SysMenuVO;
import org.sagacity.sqltoy.model.Page;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysMenuPageDTO extends Page<SysMenuVO> implements Serializable {

    private static final long serialVersionUID = -772057092053351688L;

    @NotNull(message = "查询参数为空")
    private SysMenuVO sysMenu;

    public SysMenu getPO() {
        return Convert.convert(SysMenu.class, sysMenu);
    }
}
