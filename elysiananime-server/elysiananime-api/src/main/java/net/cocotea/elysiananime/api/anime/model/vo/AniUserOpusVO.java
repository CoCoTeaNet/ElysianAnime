package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
* 用户作品关联表
*
* @author CoCoTea 572315466@qq.com
* @version 2.0.0
*/
@Data
@Accessors(chain = true)
public class AniUserOpusVO implements Serializable {

	@Serial
	private static final long serialVersionUID = -4540187768497495045L;

	/**
	 * 用户作品关联表id
	 */
	private BigInteger id;

	/**
	 * 作品ID
	 */
	private BigInteger opusId;

	/**
	 * 作品名称
	 */
	private String nameCn;

	/**
	 * 作品原名
	 */
	private String nameOriginal;

	/**
	 * 封面路径
	 */
	private String coverUrl;

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
	private BigInteger readingTime;

	/**
	 *观看状态：0未看 1已看 2在看
	 */
	private Integer readStatus;

	/**
	 *是否分享：0否 1是
	 */
	private Integer isShare;

	/**
	 * 关联时间
	 */
	private LocalDateTime createTime;
}