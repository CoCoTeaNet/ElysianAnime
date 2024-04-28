import {request, post, get} from '@/utils/axios-util';

/**
 * 新增任务
 */
export function add(data: any) {
    return request('schedule/job/add', data, post);
}

/**
 * 更新任务
 * @param data
 * @returns {Promise<any>}
 */
export function update(data: any) {
    return request('schedule/job/update', data, post);
}

/**
 * 批量删除任务
 * @param data
 * @returns {Promise<*>}
 */
export function deleteBatch(data: string[]) {
    return request('schedule/job/deleteBatch', data, post);
}

/**
 * 任务列表
 */
export function listByPage(data: any) {
    return request('schedule/job/listByPage', data, post);
}

export function execute(data: any) {
    return request('schedule/job/execute', data, get);
}

export function queryProgress(data: any) {
    return request('schedule/job/progress', data, get);
}
