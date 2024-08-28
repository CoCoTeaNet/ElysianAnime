package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.api.anime.model.po.AniTag;

import java.math.BigInteger;
import java.util.List;

@Data
@Accessors(chain = true)
public class AniUserOpusSharesVO {

    /**
     * 作品ID
     */
    private BigInteger opusId;

    /**
     * 作品名称
     */
    private String opusName;

    /**
     * 作品名称（原名）
     */
    private String opusNameOriginal;

    /**
     * 作品简介
     */
    private String opusSummary;

    /**
     * 作品详细链接
     */
    private String detailInfoUrl;

    /**
     * 放送开始
     */
    private String launchStart;

    /**
     * 放送星期
     */
    private String deliveryWeek;

    /**
     * 作品封面地址
     */
    private String coverUrl;

    /**
     * 分享的用户列表
     */
    private List<String> shareUserList;

    /**
     * 作品标签
     */
    private List<AniTag> aniTagList;

    private String shareUser;

}
