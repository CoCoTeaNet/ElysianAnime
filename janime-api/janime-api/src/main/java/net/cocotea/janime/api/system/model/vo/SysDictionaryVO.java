package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysDictionaryVO {

    private BigInteger id;
    private BigInteger parentId;
    private String dictionaryName;
    private String remark;
    private Integer sort;
    private Integer enableStatus;
    private String createBy;
    private LocalDateTime createTime;

    private List<SysDictionaryVO> children;

}
