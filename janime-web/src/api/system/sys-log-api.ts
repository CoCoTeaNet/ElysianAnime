import {post, request} from '@/utils/axios-util';

const sysLogApi = {
    listByPage: listByPage,
    deleteBatch: deleteBatch
};

/**
 * 分页获取日志
 * @param data
 */
export function listByPage(data: any) {
    return request('system/log/listByPage', data, 'POST');
}


/**
 * 批量删除日志
 * @param data
 */
export function deleteBatch(data: any) {
    return request('system/log/deleteBatch', data, post);
}

export default sysLogApi;