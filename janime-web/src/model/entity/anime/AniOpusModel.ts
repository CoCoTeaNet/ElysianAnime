/**
 * @author CoCoTea 572315466@qq.com
 * @version 2.0.3
 * 作品
 */
interface AniOpusModel {
    id?: string,
    // 原名
    nameOriginal?: string,
    // 中文名
    nameCn?: string,
    // 封面地址
    coverUrl?: string,
    // 作品类型：1动画 2漫画 3游戏 4音乐 5电影 6电视剧
    opusType?: string,
    // 详细链接
    detailInfoUrl?: string,
    // rss订阅链接
    rssUrl?: string,
    // 订阅状态: 0未订阅 1订阅中 2订阅完成
    rssStatus?: number,
    // 资源标题集数出现的位置（数字匹配）
    rssLevelIndex?: number,
    // 要订阅的资源格式
    rssFileType?: string,
    // 匹配的唯一标识
    rssOnlyMark?: string,
    // 排除的资源标识
    rssExcludeRes?: string,
}