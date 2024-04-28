package net.cocotea.janime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
* 用户作品关联表
*
* @author CoCoTea 572315466@qq.com
* @since 1.2.1 2023-03-13
*/
@Data
@Accessors(chain = true)
public class AniUserOpusAddDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *用户id
	 */
	private String userId;

	/**
	 *作品id
	 */
	private String opusId;

	/**
	 *资源地址
	 */
	private String resourceUrl;

	/**
	 *正在的播放集数
	 */
	private Integer readingNum;

	/**
	 *正在播放的时长
	 */
	private Long readingTime;

	/**
	 *观看状态：0未看 1已看 2在看
	 */
	private Integer readStatus;

	/**
	 *关联时间
	 */
	private Date createTime;

	/**
	 *是否分享：0否 1是
	 */
	private Integer isShare;

}