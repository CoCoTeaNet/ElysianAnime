package net.cocotea.janime.api.anime.rss.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MkXmlDetail {

    /**
     * 资源信息集合
     */
    List<MkXmlItem> itemList;

    /**
     * 标题碎片（按空格拆分）
     */
    private List<List<String>> titleFragmentList;

    /**
     * 集数索引（从0开始）
     */
    private List<List<String>> episodeIndexList;

}
