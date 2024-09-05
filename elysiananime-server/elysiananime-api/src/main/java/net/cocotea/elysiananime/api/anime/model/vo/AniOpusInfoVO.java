package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AniOpusInfoVO extends AniOpus {
}
