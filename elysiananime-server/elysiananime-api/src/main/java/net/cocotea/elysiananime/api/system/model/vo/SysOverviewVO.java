package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统数据概览
 *
 * @author CoCoTea
 */
@Data
@Accessors(chain = true)
public class SysOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5798937800323621804L;

    /**
     * 标题
     */
    private String title;

    /**
     * 数量
     */
    private Long count;

}
