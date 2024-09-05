import {request, post, get} from '@/utils/axios-util';

const sysThemeApi = {
	updateByUser: updateByUser,
	loadByUser: loadByUser,
};

export function updateByUser(data: any) {
	return request('system/theme/updateByUser', data, post);
}

export function loadByUser() {
	return request('system/theme/loadByUser', {}, get);
}


export default sysThemeApi;
