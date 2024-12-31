package net.cocotea.elysiananime.api.anime.model.vo;

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
     * 用户正在观看的集数
     */
    private Integer readingNum;
    /**
     * 用户正在观看的时长
     */
    private BigInteger readingTime;
    /**
     * 观看状态：0未看 1已看 2在看
     */
    private Integer readStatus;
    /**
     * 下载资源数量
     */
    private Integer downloadNum;

}
