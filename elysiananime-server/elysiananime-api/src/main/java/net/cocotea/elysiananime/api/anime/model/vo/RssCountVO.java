package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * RSS数量统计
 */
@Data
@Accessors(chain = true)
public class RssCountVO {

    /**
     * 订阅中数量
     */
    private Long subscribing;

    /**
     * 订阅完成数量
     */
    private Long subscriptionCompleted;

}
