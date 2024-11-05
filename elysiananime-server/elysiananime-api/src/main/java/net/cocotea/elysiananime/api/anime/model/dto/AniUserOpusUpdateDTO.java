package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

/**
* 用户作品关联表
*
* @author CoCoTea 572315466@qq.com
* @version 2.0.0
*/
@Data
@Accessors(chain = true)
public class AniUserOpusUpdateDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = -7252647674007876814L;

	/**
	 * 用户关联作品id
	 */
	@NotNull(message = "关联id为空")
	private BigInteger id;

	/**
	 *资源地址
	 */
	private String resourceUrl;

	/**
	 *正在的播放集数
	 */
	private String readingNum;

	/**
	 *正在播放的时长
	 */
	private Long readingTime;

	/**
	 *观看状态：0未看 1已看 2在看
	 */
	private Integer readStatus;

	/**
	 *是否分享：0否 1是
	 */
	private Integer isShare;

}