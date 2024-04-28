package net.cocotea.janime.api.anime.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.api.anime.model.vo.AniOpusVO;
import net.cocotea.janime.api.anime.model.vo.AniUserOpusVO;
import org.sagacity.sqltoy.model.Page;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AniOpusPageDTO extends Page<AniOpusVO> implements Serializable {
    private static final long serialVersionUID = -1L;

    @NotNull(message = "传参有误")
    private AniOpusVO aniOpus;

    /**
     * 搜索字符串
     */
    private String searchKey;

    /**
     * 季度
     */
    private String quarter;

}