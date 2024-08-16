package net.cocotea.janime.api.anime.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.common.model.ApiPageDTO;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AniOpusPageDTO extends ApiPageDTO implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 查询参数
     */
    @NotNull(message = "查询参数为空")
    private Query aniOpus;

    /**
     * 搜索字符串
     */
    private String searchKey;

    /**
     * 季度
     */
    private String quarter;

    @Data
    public static class Query {
        /**
         * 订阅状态: 0未订阅 1订阅中 2订阅完成
         */
        private Integer rssStatus;
        /**
         * 是否有资源
         */
        private Integer hasResource;
        /**
         *原名
         */
        private String nameOriginal;
        /**
         *中文名
         */
        private String nameCn;
    }

}