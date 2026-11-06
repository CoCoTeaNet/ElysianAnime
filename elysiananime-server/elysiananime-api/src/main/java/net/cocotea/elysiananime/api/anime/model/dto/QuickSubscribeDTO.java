package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;

/**
 * 快速订阅参数
 */
@Data
@Accessors(chain = true)
public class QuickSubscribeDTO {

    /**
     * 蜜柑番剧 RSS 链接
     */
    @NotBlank(message = "rss订阅链接为空")
    private String rssUrl;

}
