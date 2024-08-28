package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysRoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7530186405143341065L;

    /**
     * 角色ID
     */
    private String id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色键值
     */
    private String roleKey;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 创建人
     */
    private BigInteger createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private BigInteger updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
