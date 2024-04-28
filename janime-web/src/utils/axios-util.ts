import axios from "axios";
import {store} from "@/store";
import {ApiResultEnum} from "@/api/ApiResultEnum";
import {router} from "@/router";

export const post = 'POST';

export const get = 'GET';

/**
 * 封装axios
 * @param url 请求地址
 * @param data 参数
 * @param method 请求方法
 */
export async function request(url: string, data: any, method: any): Promise<any> {
    let res: any = await axios.request({
            // `url` 是用于请求的服务器 URL
            url: `/api/${url}`,
            // `method` 是创建请求时使用的方法 (默认是 get)
            method: method,
            // `transformRequest` 允许在向服务器发送前，修改请求数据
            transformRequest: [function (data: any) {
                // 对 data 进行任意转换处理
                return JSON.stringify(data);
            }],
            // `transformResponse` 在传递给 then/catch 前，允许修改响应数据
            transformResponse: [function (data: any) {
                // 对 data 进行任意转换处理
                let dataJson = JSON.parse(data);
                // 无权限，跳转admin首页
                if (dataJson.code === ApiResultEnum.NOT_PERMISSION) {
                    console.log('go to home')
                    router.push({name: 'Home'});
                }
                return dataJson;
            }],
            // `headers` 是即将被发送的自定义请求头
            headers: {
                "X-Requested-With": "XMLHttpRequest",
                "Content-Type": 'application/json;charset=utf-8',
                "sa-token": store.state.userInfo.token ? store.state.userInfo.token : 'sa-token'
            },
            // `params` 是即将与请求一起发送的 URL 参数
            params: method === 'GET' ? data : '',
            // `data` 是作为请求主体被发送的数据
            data: method === 'GET' ? null : data,
            // 如果请求花费了超过 `timeout` 的时间，请求将被中断;`timeout` 指定请求超时的毫秒数(0 表示无超时时间)
            timeout: 60000,
            // `responseType` 表示服务器响应的数据类型，可以是 "arraybuffer", "blob", "document", "json", "text", "stream"
            responseType: "json", // 默认的
            // `maxContentLength` 定义允许的响应内容的最大尺寸
            maxContentLength: 2000,
            // `validateStatus` 定义对于给定的HTTP 响应状态码是 resolve 或 reject  promise 。
            // 如果 `validateStatus` 返回 `true` (或者设置为 `null` 或 `undefined`)，promise 将被 resolve;
            // 否则，promise 将被 reject
            validateStatus: function (status: number) {
                return status === 200;
            }
        }
    );

    return res.data;
}

export async function requestFile(url: string, data: any, method: any): Promise<any> {
    let res: any = await axios.request({
            // `url` 是用于请求的服务器 URL
            url: `/api/${url}`,
            // `method` 是创建请求时使用的方法 (默认是 get)
            method: method,
            // `transformRequest` 允许在向服务器发送前，修改请求数据
            transformRequest: [function (data: any) {
                // 对 data 进行任意转换处理
                return JSON.stringify(data);
            }],
            // `headers` 是即将被发送的自定义请求头
            headers: {
                "X-Requested-With": "XMLHttpRequest",
                "Content-Type": 'application/json;charset=utf-8',
                "sa-token": store.state.userInfo.token ? store.state.userInfo.token : 'sa-token'
            },
            // `params` 是即将与请求一起发送的 URL 参数
            params: method === 'GET' ? data : '',
            // `data` 是作为请求主体被发送的数据
            data: method === 'GET' ? null : data,
            // `responseType` 表示服务器响应的数据类型，可以是 "arraybuffer", "blob", "document", "json", "text", "stream"
            responseType: "blob",
        }
    );

    return res.data;
}
