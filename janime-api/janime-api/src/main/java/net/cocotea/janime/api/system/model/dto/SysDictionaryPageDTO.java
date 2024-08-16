package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.api.system.model.vo.SysDictionaryVO;
import org.sagacity.sqltoy.model.Page;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典分页参数
 * @author CoCoTea
 * @date 2022-3-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysDictionaryPageDTO extends Page<SysDictionaryVO> implements Serializable {
    @Serial
    private static final long serialVersionUID = 8818887425379309640L;

    /**
     *
     */
    private SysDictionaryVO sysDictionary;
}
