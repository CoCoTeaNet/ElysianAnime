package net.cocotea.elysiananime.common.model;

import lombok.Data;
import org.noear.solon.validation.annotation.Min;
import org.noear.solon.validation.annotation.NotNull;

/**
 * @author CoCoTea
 * @version 2.0.6
 */
@Data
public class ApiPageDTO {

    /**
     * 页码
     */
    @NotNull(message = "页码为空")
    @Min(value = 1, message = "页码最小值为1")
    private int pageNo;

    /**
     * 页大小
     */
    @NotNull(message = "页大小为空")
    @Min(value = 1, message = "页大小最小值为1")
    private int pageSize;

}
