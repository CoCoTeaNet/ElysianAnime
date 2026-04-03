import { request } from '@/utils/axios-util';

/**
 * 获取数量统计
 */
export function getCount() {
    return request('system/dashboard/getCount', {}, 'GET');
}
