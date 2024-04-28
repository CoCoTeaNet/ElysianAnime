package net.cocotea.janime.api.anime.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AniVideoVO extends AniOpusVO {

    private static final long serialVersionUID = -4648364767431986669L;

    /**
     * 集数
     */
    private List<Media> mediaList;
    /**
     * 观看状态 {@link net.cocotea.janime.common.enums.ReadStatusEnum}
     */
    private Integer readStatus;
    /**
     * 正在观看集数
     */
    private Integer readingNum;
    /**
     * 正在观看的进度
     */
    private BigInteger readingTime;
    /**
     * 是否追番
     */
    private Integer isFollow;
    /**
     * 是否推荐
     */
    private Integer isShare;
    /**
     * 用户作品关联ID
     */
    private BigInteger userOpusId;

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
    }

}
