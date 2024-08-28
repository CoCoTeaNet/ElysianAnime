/**
 * 分页参数
 */
interface PageParam {
    // 页码
    pageNo: number,
    // 页大小
    pageSize: number,
    // 搜索字符串
    searchKey?: string,
    // 搜索对象
    searchObject: any
}