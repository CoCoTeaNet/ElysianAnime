package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysDictionaryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8024923553149431446L;

    /**
     * 字典ID
     */
    private BigInteger id;

    /**
     * 父节点ID
     */
    private BigInteger parentId;

    /**
     * 字典名称
     */
    private String dictionaryName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序序号
     */
    private Integer sort;

    /**
     * 启用状态;0关闭 1启用
     */
    private Integer enableStatus;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 子项
     */
    private List<SysDictionaryVO> children;

}
