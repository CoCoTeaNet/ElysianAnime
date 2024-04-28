package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysRoleVO {

    private BigInteger id;
    private String roleName;
    private String roleKey;
    private String remark;
    private Integer sort;
    private BigInteger createBy;
    private LocalDateTime createTime;
    private BigInteger updateBy;
    private LocalDateTime updateTime;

}
