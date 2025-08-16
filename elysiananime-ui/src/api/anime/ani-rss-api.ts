import { request, post, get } from '@/utils/axios-util';

const rssApi = {
	closeSubscribe: closeSubscribe, addOpusTorrent
}

export function rssSubscribe(data: any) {
	return request('anime/rss/subscribe', data, post);
}

export function closeSubscribe(opusId: any) {
	return request(`anime/rss/${opusId}/closeSubscribe`, {}, post);
}

export function getMkXmlDetail(rssUrl: string) {
	return request(`anime/rss/getMkXmlDetail?rssUrl=${rssUrl}`, {}, get);
}

/**
 * 获取RSS运行状态
 */
export function getRssWorkStatus() {
	return request('anime/rss/getRssWorkStatus', {}, get);
}

export function getRenames(data: any) {
	return request('anime/rss/getRenames', data, post);
}

export function defaultExclusions() {
	return request('anime/rss/defaultExclusions', {}, get);
}

export function addOpusTorrent(data: any) {
	return request('anime/rss/addOpusTorrent', data, post);
}

export default rssApi;
