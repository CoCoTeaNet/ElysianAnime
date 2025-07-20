package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.api.anime.model.po.AniTag;

import java.io.Serial;
import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
public class AniOpusTagVO extends AniTag {

    @Serial
    private static final long serialVersionUID = 4341764486879858948L;

    private BigInteger opusId;

}
