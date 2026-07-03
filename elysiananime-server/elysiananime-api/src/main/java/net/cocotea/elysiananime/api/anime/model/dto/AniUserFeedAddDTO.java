package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AniUserFeedAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "作品ID为空")
    private BigInteger opusId;

    @NotNull(message = "被投喂用户ID为空")
    private BigInteger toUserId;
}
