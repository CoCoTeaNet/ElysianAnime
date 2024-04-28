import {request, requestFile, post, get} from '@/utils/axios-util';

export function update(data?: any) {
	return request('system/file/update', data, post);
}

export function download(fileId?: string) {
	return requestFile(`system/file/download/${fileId}`, {}, get);
}

export function deleteBatch(data: any) {
	return request('system/file/deleteBatch', data, post);
}

export function listByPage(data: any) {
	return request('system/file/listByPage', data, post);
}

/**
 * 回收站分页接口
 * @param data
 */
export function recycleBinPage(data: any) {
	return request('system/file/recycleBinPage', data, post);
}

/**
 * 批量删除回收站的文件接口
 * @param data
 */
export function recycleBinDeleteBatch(data: any) {
	return request('system/file/recycleBin/deleteBatch', data, post);
}

/**
 * 批量恢复文件接口
 * @param data
 */
export function recoveryBatch(data: any) {
	return request('system/file/recycleBin/recoveryBatch', data, post);
}