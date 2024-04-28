/**
* @author CoCoTea 572315466@qq.com
* @since 1.2.1 2023-03-13
* 用户作品关联表
*/
interface AcgUserOpusModel {

    // 用户id
    userId?: string,
    // 作品id
    opusId?: string,
    // 资源地址
    resourceUrl?: string,
    // 正在的播放集数
    readingNum?: string,
    // 正在播放的时长
    readingTime?: string,
    // 观看状态：0未看 1已看 2在看
    readStatus?: string,
    // 关联时间
    createTime?: string,
    // 是否分享：0否 1是
    isShare?: string,

}