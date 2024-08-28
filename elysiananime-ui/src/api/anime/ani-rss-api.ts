import {request, post, get} from '@/utils/axios-util';

export function rssSubscribe(data: any) {
	return request('anime/rss/subscribe', data, post);
}

export function closeSubscribe(data: any) {
	return request(`anime/rss/${data}/closeSubscribe`, {}, post);
}

export function getMkXmlDetail(rssUrl: string) {
	return request(`anime/rss/getMkXmlDetail?rssUrl=${rssUrl}`, {}, get);
}
