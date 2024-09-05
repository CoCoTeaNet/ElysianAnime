package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RssWorksStatusDTO {

    /**
     * 最后运行时间
     */
    private String lastExecTime;

    /**
     * 执行消息
     */
    private String execMessage;

}
