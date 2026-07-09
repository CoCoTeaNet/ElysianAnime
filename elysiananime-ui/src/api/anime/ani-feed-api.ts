import {request, post, get} from '@/utils/axios-util';

export function feed(data: any) {
    return request('anime/feed/add', data, post);
}

export function feedListByUser(data: any) {
    return request('anime/feed/listByUser', data, post);
}

export function feedListSent(data: any) {
    return request('anime/feed/listSent', data, post);
}

export function deleteFeed(id: string) {
    return request(`anime/feed/delete/${id}`, {}, post);
}
