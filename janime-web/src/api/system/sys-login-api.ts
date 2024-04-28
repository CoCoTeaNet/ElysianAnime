import {request} from '@/utils/axios-util';

/**
 * 获取验证码
 */
export function getCaptcha(data: any) {
    return request('system/captcha', data, 'POST');
}


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