package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.api.system.model.vo.SysFileVO;
import org.sagacity.sqltoy.model.Page;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysFilePageDTO extends Page<SysFileVO> implements Serializable {
    private static final long serialVersionUID = -1L;

    @NotNull(message = "查询参数为空")
    private SysFileVO sysFile;

    private Integer isDeleted;
}