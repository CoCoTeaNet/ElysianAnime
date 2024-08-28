import { request } from '@/utils/axios-util';

/**
 * 获取数量统计
 */
export function getCount() {
    return request('system/dashboard/getCount', {}, 'GET');
}

/**
 * 获取系统信息
 */
export function getSystemInfo() {
    return request('system/dashboard/getSystemInfo', {}, 'GET');
}

/**
 * 获取RSS运行状态
 */
export function getRssWorkStatus() {
    return request('system/dashboard/getRssWorkStatus', {}, 'GET');
}