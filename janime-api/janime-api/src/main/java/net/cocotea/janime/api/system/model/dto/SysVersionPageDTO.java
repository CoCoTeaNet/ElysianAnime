package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.api.system.model.vo.SysVersionVO;
import org.sagacity.sqltoy.model.Page;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysVersionPageDTO extends Page<SysVersionVO> implements Serializable {
	private static final long serialVersionUID = 2569886533383934522L;

	@NotNull(message = "查询参数为空")
	private SysVersionVO sysVersion;

}
