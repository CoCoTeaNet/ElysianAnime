import {request} from '@/utils/axios-util';

const roleApi = {
    listByPage: listByPage,
    add: add,
    deleteBatch: deleteBatch,
    update: update,
    grantPermissionsByRoleId: grantPermissionsByRoleId
}

/**
 * 新增角色
 */
export function add(data: any) {
    return request('system/role/add', data, 'POST');
}

/**
 * 批量删除角色
 */
export function deleteBatch(data: any) {
    return request('system/role/deleteBatch', data, 'POST');
}

/**
 * 更新角色
 */
export function update(data: any) {
    return request('system/role/update', data, 'POST');
}

/**
 * 分页获取角色
 */
export function listByPage(data: any) {
    return request('system/role/listByPage', data, 'POST');
}

/**
 * 给角色分配权限
 */
export function grantPermissionsByRoleId(data: any) {
    return request('system/role/grantPermissionsByRoleId', data, 'POST');
}

export default roleApi;