package net.cocotea.elysiananime.api;

import net.cocotea.elysiananime.common.model.ApiResult;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;

/**
 * 系统首页接口
 * @author CoCoTea
 * @version 3.0.0
 */
@Controller
public class HomeController {

    /**
     * 查看系统健康状态
     */
    @Mapping(value = "health", method = MethodType.GET)
    public ApiResult<String> health() {
        return ApiResult.ok("OK");
    }

    /**
     * 默认首页
     */
    @Mapping(value = "/", method = MethodType.GET)
    public void home(Context ctx) {
        //在服务端重新路由到 /index.html （浏览器发生1次请求，地址不会变）
        ctx.forward("/index.html");
    }

}
