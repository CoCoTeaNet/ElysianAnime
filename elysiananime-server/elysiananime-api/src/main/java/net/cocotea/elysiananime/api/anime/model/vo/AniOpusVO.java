package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.api.anime.model.po.AniTag;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
* 作品
*
* @author CoCoTea 572315466@qq.com
* @since 1.2.1 2023-03-07
*/
@Data
@Accessors(chain = true)
public class AniOpusVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;

	/**
	 *原名
	 */
	private String nameOriginal;

	/**
	 *中文名
	 */
	private String nameCn;

	/**
	 *封面地址
	 */
	private String coverUrl;

	/**
	 *详细链接
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
	 * rss订阅链接
	 */
	private String rssUrl;

	/**
	 * 订阅状态: 0未订阅 1订阅中 2订阅完成
	 */
	private Integer rssStatus;

	/**
	 * 资源标题集数出现的位置（数字匹配）
	 */
	private Integer rssLevelIndex;

	/**
	 * 要订阅的资源格式
	 */
	private String rssFileType;

	/**
	 * 匹配的唯一标识
	 */
	private String rssOnlyMark;

	/**
	 * 排除的资源
	 */
	private String rssExcludeRes;

	/**
	 * acg标签
	 */
	private List<AniTag> aniTags;

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
	 * 季度
	 */
	private List<String> quarterList;

	/**
	 * 简介
	 */
	private String aniSummary;

}