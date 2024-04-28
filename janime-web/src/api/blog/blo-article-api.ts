import {request, post, get} from '@/utils/axios-util';

/**
 * 新增文章
 */
export function add(data: any) {
    return request('cms/article/add', data, post);
}

/**
 * 更新文章
 * @param data
 * @returns {Promise<any>}
 */
export function update(data: any) {
    return request('cms/article/update', data, post);
}

/**
 * 批量删除文章
 * @param data
 * @returns {Promise<*>}
 */
export function deleteBatch(data: string[]) {
    return request('cms/article/deleteBatch', data, post);
}

/**
 * 文章列表
 */
export function listByPage(data: any) {
    return request('cms/article/listByPage', data, post);
}

/**
 * 文章详细
 */
export function detail(id: string) {
    return request(`cms/article/detail/${id}`, {}, get);
}