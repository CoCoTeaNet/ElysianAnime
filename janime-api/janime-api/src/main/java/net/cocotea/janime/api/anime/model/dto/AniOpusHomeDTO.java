package net.cocotea.janime.api.anime.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.model.Page;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AniOpusHomeDTO extends Page<AniOpusHomeDTO> implements Serializable {

    private static final long serialVersionUID = 5725962847791361253L;

    /**
     * 是否有资源
     */
    private Integer hasResource;
    /**
     * 搜索字符串
     */
    private String searchKey;
    /**
     * 追番进度
     */
    private List<String> status;
    /**
     * 季度
     */
    private List<String> months;
    /**
     * 完结状态
     */
    private List<String> states;
    /**
     * 年份
     */
    private List<String> years;
    /**
     * 周
     */
    private List<String> deliveryWeeks;
    /**
     * 作品id
     */
    private List<BigInteger> opusIds;

}