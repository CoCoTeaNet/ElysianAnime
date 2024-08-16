package net.cocotea.janime.api.anime.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AniUserOpusPageDTO extends AniOpusPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5239736941826443862L;

    /**
     * 原名
     */
    private String nameOriginal;

    /**
     * 中文名
     */
    private String nameCn;

    /**
     * 观看状态：0未看 1已看 2在看
     */
    private Integer readStatus;

    /**
     * 是否分享
     */
    private Integer isShare;

}