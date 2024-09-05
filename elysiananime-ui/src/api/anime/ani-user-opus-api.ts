import {request, post, get} from '@/utils/axios-util';

const userOpusApi = {
    follow: follow,
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

export function updateProgress(data: any) {
    return request('anime/userOpus/updateProgress', data, post);
}

export function share(opusId: string) {
    return request(`anime/userOpus/share/${opusId}`, {}, post);
}

export function sharesList(limits: number) {
    return request(`anime/userOpus/shares/list?limits=${limits}`, {}, get);
}

export default userOpusApi;