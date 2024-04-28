package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.api.system.model.vo.SysMenuVO;
import net.cocotea.janime.api.system.model.vo.SysRoleVO;
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
public class SysRolePageDTO extends Page<SysMenuVO> implements Serializable {

	private static final long serialVersionUID = -8722226920902960302L;

	@NotNull(message = "查询参数为空")
	private SysRoleVO sysRole;
}
