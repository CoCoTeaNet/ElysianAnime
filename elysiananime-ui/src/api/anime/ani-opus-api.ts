import {request, post, get} from '@/utils/axios-util';

const opusApi = {
	update: update,
}

export function add(data: any) {
	return request('anime/opus/add', data, post);
}

export function deleteBatch(data: any) {
	return request('anime/opus/deleteBatch', data, post);
}

export function update(data: any) {
	return request('anime/opus/update', data, post);
}

export function listByPage(data: any) {
	return request('anime/opus/listByPage', data, post);
}

export function listByUser(data: any) {
	return request('anime/opus/listByUser', data, post);
}

export function getOpusMedia(data: any) {
	return request(`anime/opus/getOpusMedia/${data}`, {}, get);
}

export function addOpusFromBangumi(data: any) {
	return request(`anime/opus/addOpusFromBangumi`, data, post);
}

export function getMedia(opus: any) {
	return request(`anime/opus/media/${opus.id}?resName=${opus.resName}`, {}, get);
}

export default opusApi;
