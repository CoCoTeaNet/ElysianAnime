package net.cocotea.elysiananime.api.system.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.common.model.ApiPageDTO;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysFilePageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4330604145822899773L;

    /**
     * 文件信息查询参数
     */
    @NotNull(message = "查询参数为空")
    private Query sysFile;

    /**
     * 是否删除：{@link net.cocotea.elysiananime.common.enums.IsEnum}
     */
    private Integer isDeleted;

    @Data
    public static class Query {
        /**
         *文件后缀
         */
        private String fileSuffix;

        /**
         *文件名称
         */
        private String fileName;

        /**
         * 开始时间
         */
        private LocalDateTime beginTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;
    }

}