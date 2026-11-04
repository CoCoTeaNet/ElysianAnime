package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AniVideoVO extends AniOpusVO {

    @Serial
    private static final long serialVersionUID = -4648364767431986669L;

    /**
     * 集数
     */
    private List<Media> mediaList;
    /**
     * 观看状态 {@link net.cocotea.elysiananime.common.enums.ReadStatusEnum}
     */
    private Integer readStatus;
    /**
     * 是否追番
     */
    private Integer isFollow;
    /**
     * 是否推荐
     */
    private Integer isShare;
    /**
     * 最新一集的逻辑集数键
     */
    private String mediaLatestKey;
    /**
     * 媒体文件数
     */
    private Integer mediaCount;
    /**
     * 最近观看的集数键，未追番或还没看过时为 null
     */
    private String lastEpisodeKey;
    /**
     * 最近观看的媒体文件id，未追番或还没看过时为 null
     */
    private BigInteger lastMediaId;
    /**
     * 按集进度：key = episodeKey，value = 该集进度
     */
    private Map<String, AniEpisodeProgressVO> progressMap;

    @Data
    @Accessors(chain = true)
    public static class Media {
        /**
         * 集数
         */
        private String episodes;
        /**
         * 资源格式
         */
        private String mediaType;
        /**
         * 是否文件夹
         */
        private Integer isFolder;
        /**
         * 媒体文件id
         */
        private BigInteger mediaId;
        /**
         * 逻辑集数键
         */
        private String episodeKey;
        /**
         * 集数，无集数文件为 null
         */
        private BigDecimal episodeNo;
        /**
         * 是否额外内容（SP / OVA / 剧场版 / PV / 无集数）
         */
        private Integer isExtra;
        /**
         * 排序号，无集数文件为 null
         */
        private BigDecimal sortNo;
        /**
         * 展示名
         */
        private String displayName;
    }

}
