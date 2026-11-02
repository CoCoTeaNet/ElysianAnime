package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 继续观看条目
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Data
@Accessors(chain = true)
public class AniContinueVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7712894302155198301L;

    /**
     * 作品ID
     */
    private BigInteger opusId;
    /**
     * 中文名
     */
    private String nameCn;
    /**
     * 封面地址
     */
    private String coverUrl;
    /**
     * 最近观看的集数键
     */
    private String episodeKey;
    /**
     * 集数展示名
     */
    private String displayName;
    /**
     * 最近观看的媒体文件ID
     */
    private BigInteger mediaId;
    /**
     * 播放位置（毫秒）
     */
    private Long positionMs;
    /**
     * 视频总时长（毫秒），未知为 0
     */
    private Long durationMs;
    /**
     * 最近观看时间
     */
    private LocalDateTime lastReadTime;

}
