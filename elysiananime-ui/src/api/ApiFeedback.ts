import {ElMessage} from "element-plus";
import {router} from "@/router";
import {ApiResultEnum} from "@/api/ApiResultEnum";

// 控制未登录的时候很多错误弹窗请求
let isLostSession: boolean = true;

/**
 * api请求反馈模板
 * @param apiFn api函数
 * @param successMsg 成功消息
 * @param errorMsg 失败消息
 * @param successCallback 成功回调
 */
export function reqFeedback(apiFn: any, successMsg: string, errorMsg: string, successCallback: Function) {
    apiFn.then((res: any) => {
        if (res.code === ApiResultEnum.SUCCESS) {
            if (successMsg) {
                ElMessage.success(successMsg);
            }
            successCallback(res.data);
        } else if (res.code === ApiResultEnum.NOT_LOGIN || res.code === ApiResultEnum.TOKEN_INVALID) {
            if (isLostSession) {
                router.push({name: 'Login'}).then((r: any) => {
                    ElMessage({
                        message: res.message,
                        type: 'warning',
                    });
                    isLostSession = true;
                });
                isLostSession = false;
                // 清除本地用户信息
                localStorage.removeItem("userInfo");
            }
        } else {
            if (errorMsg) {
                ElMessage.error(errorMsg);
                return;
            }
            if (!res.data) {
                ElMessage.error(res.message);
            } else {
                ElMessage.error(res.data);
            }
        }
    }).catch((e: any) => {
        console.log(e)
    });
}

export function reqSuccessFeedback(apiFn: any, msg: string, successCallback: Function) {
    reqFeedback(apiFn, msg, '', successCallback);
}

/**
 * 常用api请求反馈模板
 */
export function reqCommonFeedback(apiFn: any, successCallback: Function) {
    reqFeedback(apiFn, '', '', successCallback);
}