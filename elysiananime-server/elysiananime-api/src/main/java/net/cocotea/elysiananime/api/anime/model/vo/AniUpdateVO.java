package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 作品更新动态条目
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Data
@Accessors(chain = true)
public class AniUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8622147003045821837L;

    /**
     * 更新动态id
     */
    private BigInteger id;
    /**
     * 作品ID
     */
    private BigInteger opusId;
    /**
     * 作品中文名
     */
    private String opusName;
    /**
     * 封面地址
     */
    private String coverUrl;
    /**
     * 逻辑集数键
     */
    private String episodeKey;
    /**
     * 集数，SP/OVA 与无集数文件为空
     */
    private BigDecimal episodeNo;
    /**
     * 是否集外内容：1是 0否（episode_no 为空即为集外）
     */
    private Integer isExtra;
    /**
     * 集数展示名（媒体展示名，缺失退回集数键）
     */
    private String displayName;
    /**
     * 媒体文件ID，媒体已删除时为空
     */
    private BigInteger mediaId;
    /**
     * 来源：1扫描 2RSS 3手动上传
     */
    private Integer source;
    /**
     * 更新时间，取文件 mtime
     */
    private LocalDateTime createTime;
}
