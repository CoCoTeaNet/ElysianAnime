import {request, post, get} from '@/utils/axios-util';

/**
 * 触发单部作品的媒体扫描
 */
export function scanOpus(opusId: string) {
    return request(`anime/media/scan/${opusId}`, {}, post);
}

/**
 * 触发全量扫描
 */
export function scanAll() {
    return request('anime/media/scan', {}, post);
}

/**
 * 扫描状态
 */
export function scanStatus() {
    return request('anime/media/scan/status', {}, get);
}
