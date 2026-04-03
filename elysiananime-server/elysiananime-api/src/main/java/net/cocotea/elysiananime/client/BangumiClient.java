package net.cocotea.elysiananime.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.noear.solon.annotation.Component;

/**
 * Bangumi客户端工具，接口来源：<a href="https://github.com/bangumi/api">Api</a>
 *
 * @author CoCoTea
 * @since v2.2.2
 */
@Component
public class BangumiClient {

    /**
     * 基础URL地址
     */
    final String BASE_URL = "https://api.bgm.tv";

    /**
     * 每日放送
     */
    public JSONArray calendar() {
        String body = HttpUtil.get(BASE_URL + "/calendar");
        return JSON.parseArray(body);
    }

    /**
     * 获取条目
     *
     * @param subjectId 条目 ID
     */
    public JSONObject subjects(String subjectId) {
        String body = HttpUtil.get(BASE_URL + "/v0/subjects/" + subjectId);
        return JSON.parseObject(body);
    }

}
