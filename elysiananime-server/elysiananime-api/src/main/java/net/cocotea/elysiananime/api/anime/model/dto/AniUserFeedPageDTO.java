package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.common.model.ApiPageDTO;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AniUserFeedPageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 中文名
     */
    private String nameCn;

    /**
     * 原名
     */
    private String nameOriginal;
}
