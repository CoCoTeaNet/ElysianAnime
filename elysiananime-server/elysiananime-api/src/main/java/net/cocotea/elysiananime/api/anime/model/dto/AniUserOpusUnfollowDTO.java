package net.cocotea.elysiananime.api.anime.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * 取消追番参数
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Data
@Accessors(chain = true)
public class AniUserOpusUnfollowDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3127749301225146301L;

    /**
     * 作品ID
     */
    private BigInteger opusId;
}
