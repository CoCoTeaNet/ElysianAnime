import {request, post} from '@/utils/axios-util';

const sysUserApi = {
    getDetail: getDetail,
}

/**
 * 增加一名用户
 * @param data
 */
export function add(data: any) {
    return request('system/user/add', data, 'POST');
}

/**
 * 删除一名用户
 * @param data
 * @returns {Promise<any>}
 */
export function deleteBatch(data: any) {
    return request('system/user/deleteBatch', data, 'POST');
}

/**
 * 更新用户
 * @param data
 * @returns {Promise<any>}
 */
export function update(data: any) {
    return request('system/user/update', data, 'POST');
}

/**
 * 分页获取用户
 * @param data
 * @returns {Promise<any>}
 */
export function listByPage(data: any) {
    return request('system/user/listByPage', data, 'POST');
}

/**
 * 用户获取个人详细信息
 */
export function getDetail() {
    return request('system/user/getDetail', {}, 'GET');
}

export function updateByUser(data: any) {
    return request('system/user/updateByUser', data, 'POST');
}

/**
 * 修改个人密码
 * @param data
 * @returns {Promise<any>}
 */
export function doModifyPassword(data: any) {
    return request('system/user/doModifyPassword', data, post);
}

export default sysUserApi;
