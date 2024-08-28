package net.cocotea.elysiananime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SysDictionaryTreeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7644368722773574028L;

    /**
     * 字典名称
     */
    private String dictionaryName;

    /**
     * 启用状态;0关闭 1启用
     */
    private Integer enableStatus;

}
