import {request, post, get} from '@/utils/axios-util';

export function listByType(type: string) {
	return request(`anime/notify/listByType?type=${type}`, {}, get);
}

export function read(id: string) {
	return request(`anime/notify/read/${id}`, {}, post);
}
