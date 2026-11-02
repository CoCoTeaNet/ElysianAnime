package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单集播放进度
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Data
@Accessors(chain = true)
public class AniEpisodeProgressVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6290153784412778012L;

    /**
     * 播放位置（毫秒）
     */
    private Long positionMs;
    /**
     * 视频总时长（毫秒），未知为 0
     */
    private Long durationMs;
    /**
     * 是否看完：0否 1是
     */
    private Integer isCompleted;

}
