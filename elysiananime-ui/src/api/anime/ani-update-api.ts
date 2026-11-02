import {request, get} from '@/utils/axios-util';

/**
 * 最近更新条目（首页横滑区）
 */
export function latest(limit: number) {
    return request(`anime/update/latest?limit=${limit}`, {}, get);
}

/**
 * 更新动态分页（历史翻阅）
 */
export function updatePage(params: { pageNo?: number; pageSize?: number; opusId?: string; start?: string; end?: string }) {
    const query = new URLSearchParams();
    if (params.pageNo) query.set('pageNo', String(params.pageNo));
    if (params.pageSize) query.set('pageSize', String(params.pageSize));
    if (params.opusId) query.set('opusId', params.opusId);
    if (params.start) query.set('start', params.start);
    if (params.end) query.set('end', params.end);
    return request(`anime/update/page?${query.toString()}`, {}, get);
}
