interface MkXmlItemModel {
    guid?: string,
    link?: string,

    /**
     * 标题
     */
    title?: string,

    /**
     * 标题（内嵌浏览器标签）
     */
    titleHtml?: string,

    /**
     * 描述
     */
    description?: string,

    /**
     * bt资源地址
     */
    enclosureUrl?: string,

    /**
     * bt资源长度
     */
    enclosureLength?: string,

    torrentLink?: string,
    torrentContentLength?: string,
    torrentPubDate?: string,
}