import {request, post, get} from '@/utils/axios-util';

/**
 * 新增评论
 */
export function add(data: any) {
    return request('cms/comment/add', data, post);
}

/**
 * 更新评论
 * @param data
 * @returns {Promise<any>}
 */
export function update(data: any) {
    return request('cms/comment/update', data, post);
}

/**
 * 批量删除评论
 * @param data
 * @returns {Promise<*>}
 */
export function deleteBatch(data: string[]) {
    return request('cms/comment/deleteBatch', data, post);
}

/**
 * 评论列表
 */
export function listByPage(data: any) {
    return request('cms/comment/listByPage', data, post);
}