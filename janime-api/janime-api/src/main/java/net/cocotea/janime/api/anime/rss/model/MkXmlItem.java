package net.cocotea.janime.api.anime.rss.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author CoCoTea
 * @since v1
 * mikan rss每一集的资源信息
 */
@Data
@Accessors(chain = true)
public class MkXmlItem {

    private String guid;
    private String link;

    /**
     * 标题
     */
    private String title;

    /**
     * 标题（内嵌浏览器标签）
     */
    private String titleHtml;

    /**
     * 描述
     */
    private String description;

    /**
     * bt资源地址
     */
    private String enclosureUrl;

    /**
     * bt资源长度
     */
    private String enclosureLength;

    private String torrentLink;
    private String torrentContentLength;
    private String torrentPubDate;

}
