import {request, post, get} from '@/utils/axios-util';

const userOpusApi = {
    follow: follow,
    unfollow: unfollow,
    update: update,
    share: share,
}

export function follow(data: any) {
    return request(`anime/userOpus/${data}/follow`, {}, post);
}

export function listByUser(data: any) {
    return request('anime/userOpus/listByUser', data, post);
}

export function update(data: any) {
    return request('anime/userOpus/update', data, post);
}

/**
 * 上报播放进度（新接口，状态机）
 */
export function reportProgress(data: any) {
    return request('anime/userOpus/progress', data, post);
}

/**
 * 继续观看
 */
export function continueList() {
    return request('anime/userOpus/continue', {}, get);
}

/**
 * 取消追番
 */
export function unfollow(data: any) {
    return request('anime/userOpus/unfollow', data, post);
}

export function share(opusId: string) {
    return request(`anime/userOpus/share/${opusId}`, {}, post);
}

export function sharesList(limits: number) {
    return request(`anime/userOpus/shares/list?limits=${limits}`, {}, get);
}

export default userOpusApi;