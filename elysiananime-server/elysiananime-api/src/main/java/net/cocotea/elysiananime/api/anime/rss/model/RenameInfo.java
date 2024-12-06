package net.cocotea.elysiananime.api.anime.rss.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class RenameInfo {

    /**
     * 重命名后的结果
     */
    private String rename;

    /**
     * 标题
     */
    private String title;

    /**
     * bt资源地址
     */
    private String EnclosureUrl;

}
