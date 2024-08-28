package net.cocotea.elysiananime.api.system.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.common.model.ApiPageDTO;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统操作日志 分页查询参数
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysLogPageDTO extends ApiPageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2889574415371661414L;

    /**
     * 查询参数
     */
    @NotNull(message = "查询参数为空")
    private Query sysLog;

    @Data
    @Accessors(chain = true)
    public static class Query {
        /**
         * 日志编号
         */
        private String id;
        /**
         * 操作人
         */
        private String operator;
        /**
         * 请求方法
         */
        private String requestWay;
        /**
         * 接口路径
         */
        private String apiPath;
    }

}
