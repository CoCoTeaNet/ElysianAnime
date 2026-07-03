package net.cocotea.elysiananime.api.anime.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AniUserFeedVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 投喂ID
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
     * 投喂人ID
     */
    private BigInteger fromUserId;

    /**
     * 投喂人昵称
     */
    private String fromUserNickname;

    /**
     * 投喂时间
     */
    private LocalDateTime createTime;
}
