import {request, post, get} from '@/utils/axios-util';

/**
 * 批量删除
 * @param data
 * @returns {Promise<*>}
 */
export function deleteBatch(data: string[]) {
    return request('schedule/jobLog/deleteBatch', data, post);
}

/**
 * 列表
 */
export function listByPage(data: any) {
    return request('schedule/jobLog/listByPage', data, post);
}
