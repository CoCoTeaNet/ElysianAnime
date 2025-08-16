package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

@Accessors(chain = true)
@Data
public class AniAddOpusTorrentDTO {

    /**
     * 作品ID
     */
    @NotBlank(message = "作品ID为空")
    private String opusId;

    /**
     * 种子地址
     */
    @NotBlank(message = "种子地址为空")
    private String torrentUrl;

    /**
     * 集数
     */
    private Integer episodes;

    /**
     * 文件类型（如：mp4）
     */
    private String fileType;

}
