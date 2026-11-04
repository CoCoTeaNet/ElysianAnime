package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * 播放进度上报参数。
 * <p>
 * 报文里不接受 userId / userOpusId，用户身份只取登录态。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Data
@Accessors(chain = true)
public class AniPlaybackReportDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5104073183215481222L;

    /**
     * 作品ID
     */
    private BigInteger opusId;
    /**
     * 媒体文件ID
     */
    private BigInteger mediaId;
    /**
     * 集数键，与媒体索引的 episode_key 一致
     */
    private String episodeKey;
    /**
     * 播放位置（毫秒）
     */
    private Long positionMs;
    /**
     * 视频总时长（毫秒）
     */
    private Long durationMs;
    /**
     * 本次上报累计的观看时长（毫秒），服务端会夹到 0~15 秒
     */
    private Long watchedDeltaMs;
    /**
     * 事件：tick / pause / seek / ended / close
     */
    private String event;
    /**
     * 播放会话ID，换端/换页面会变
     */
    private String sessionId;
    /**
     * 会话内上报序号，同会话内小于已存值则丢弃
     */
    private Integer seq;

}
