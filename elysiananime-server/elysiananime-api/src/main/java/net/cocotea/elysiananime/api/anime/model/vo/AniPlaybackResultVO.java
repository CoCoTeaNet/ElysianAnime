package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * 播放进度上报结果
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Data
@Accessors(chain = true)
public class AniPlaybackResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3187225462135541123L;

    /**
     * 作品观看状态：0未看 1已看 2在看
     */
    private Integer readStatus;
    /**
     * 本集是否看完
     */
    private Integer isCompleted;
    /**
     * 作品当前最新一集的集数键
     */
    private String latestEpisodeKey;
    /**
     * 下一集的集数键，没有则为 null
     */
    private String nextEpisodeKey;
    /**
     * 下一集的媒体文件ID，没有则为 null
     */
    private BigInteger nextMediaId;
    /**
     * 本集当前的播放位置（毫秒）
     */
    private Long positionMs;

}
