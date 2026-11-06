package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.api.anime.rss.model.MkXmlDetail;

import java.math.BigInteger;

/**
 * 快速订阅解析结果
 */
@Data
@Accessors(chain = true)
public class QuickSubscribeVO {

    /**
     * 作品id（不存在时自动创建）
     */
    private BigInteger opusId;

    /**
     * 作品中文名
     */
    private String nameCn;

    /**
     * 作品原名
     */
    private String nameOriginal;

    /**
     * 封面文件名
     */
    private String coverUrl;

    /**
     * 作品详细链接
     */
    private String detailInfoUrl;

    /**
     * 作品是否已存在
     */
    private Boolean existed;

    /**
     * 已有的订阅状态
     */
    private Integer rssStatus;

    /**
     * 已有的集数位置
     */
    private Integer rssLevelIndex;

    /**
     * 已有的资源格式
     */
    private String rssFileType;

    /**
     * 已有的唯一标识
     */
    private String rssOnlyMark;

    /**
     * 已有的排除资源
     */
    private String rssExcludeRes;

    /**
     * RSS 解析结果
     */
    private MkXmlDetail detail;

}
