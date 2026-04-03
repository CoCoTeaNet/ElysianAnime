import {get, request} from '@/utils/axios-util';

/**
 * 用户登录
 */
export function login(data: any) {
    return request('system/login', data, 'POST');
}

/**
 * 获取用户登录信息
 */
export function loginInfo() {
    return request('system/loginInfo', {}, 'GET');
}

/**
 * 用户退出登录
 */
export function logout() {
    return request('system/logout', {}, 'POST');
}