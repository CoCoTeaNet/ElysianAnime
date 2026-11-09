package net.cocotea.elysiananime.api.anime.model.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * 首页作品视图
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class AniOpusHomeVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5393455671882014691L;

    /**
     * 作品ID
     */
    private BigInteger id;
    /**
     * 原名
     */
    private String nameOriginal;
    /**
     * 中文名
     */
    private String nameCn;
    /**
     * 封面地址
     */
    private String coverUrl;
    /**
     * 详细链接
     */
    private String detailInfoUrl;
    /**
     * 用户ID
     */
    private BigInteger userId;
    /**
     * 是否有资源
     */
    private Integer hasResource;
    /**
     * 话数
     */
    private String episodes;
    /**
     * 放送开始
     */
    private String launchStart;
    /**
     * 放送星期
     */
    private String deliveryWeek;
    /**
     * 观看状态：0未看 1已看 2在看
     */
    private Integer readStatus;
    /**
     * 是否已追番：1已追 0未追
     */
    private Integer isFollow;
    /**
     * 最新一集
     */
    private String episodesNewest;

    /**
     * 已看正片集数：只算当前仍存在的主文件，不含 SP 等额外内容
     */
    private Integer completedCount;
    /**
     * 当前媒体数（每集一个主文件）
     */
    private Integer mediaCount;
    /**
     * 完成度分母：主表话数，话数未知时取正片媒体数
     */
    private Integer totalEpisodes;
    /**
     * 最近观看的集数键
     */
    private String lastEpisodeKey;
    /**
     * 最近观看的媒体id
     */
    private BigInteger lastMediaId;
    /**
     * 最近观看的一集播放位置（毫秒）
     */
    private BigInteger positionMs;
    /**
     * 最近观看的一集总时长（毫秒），未知为 0
     */
    private BigInteger durationMs;
    /**
     * 是否有更新：未追上最新正片集时为 1
     */
    private Integer hasUpdate;
    /**
     * 作品级是否看完，卡片 ✓ 用
     */
    private Integer isCompleted;

    /**
     * 内部判定字段，不对外输出
     */
    @JSONField(serialize = false)
    private String mediaLatestKey;
    /**
     * 内部判定字段：最新正片集是否有已看完的进度行
     */
    @JSONField(serialize = false)
    private Integer latestEpisodeCompleted;
    /**
     * 内部判定字段：主表话数未知时用于兜底的正片媒体数
     */
    @JSONField(serialize = false)
    private Integer mainMediaCount;

}
