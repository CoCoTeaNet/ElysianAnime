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
	 * 最近观看的集数键
	 */
	private String lastEpisodeKey;

	/**
	 * 最近观看的媒体文件ID
	 */
	private BigInteger lastMediaId;

	/**
	 * 最近观看集数的展示名
	 */
	private String displayName;

	/**
	 * 最近观看集的播放位置（毫秒）
	 */
	private Long positionMs;

	/**
	 * 最近观看集的总时长（毫秒），未知为 0
	 */
	private Long durationMs;

	/**
	 * 观看状态：0未看 1已看 2在看
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