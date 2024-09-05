package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
* 作品
*
* @author CoCoTea 572315466@qq.com
* @since 1.2.1 2023-03-07
*/
@Data
@Accessors(chain = true)
public class AniOpusAddDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String ID;

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
	 *作品类型：1动画 2漫画 3游戏 4音乐 5电影 6电视剧
	 */
	private Integer opusType;

	/**
	 *详细链接
	 */
	private String detailInfoUrl;

	private Date createTime;

	/**
	 *创建人
	 */
	private String createBy;

	/**
	 *更新时间
	 */
	private Date updateTime;

	/**
	 *更新人
	 */
	private String updateBy;

}