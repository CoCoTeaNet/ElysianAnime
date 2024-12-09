package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import java.math.BigInteger;

/**
 * rss订阅参数
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class AniRssDTO {

    /**
     * 作品id
     */
    @NotNull(message = "作品id为空")
    private BigInteger id;

    /**
     * rss订阅链接
     */
    @NotBlank(message = "rss订阅链接为空")
    private String rssUrl;

    /**
     * 资源标题集数出现的位置（数字匹配）
     */
    @NotNull(message = "资源标题集数出现的位置为空")
    private Integer rssLevelIndex;

    /**
     * 要订阅的资源格式
     */
    @NotBlank(message = "要订阅的资源格式为空")
    private String rssFileType;

    /**
     * 匹配的唯一标识
     */
    @NotBlank(message = "匹配的唯一标识为空")
    private String rssOnlyMark;

    private String rssExcludeRes;

}
