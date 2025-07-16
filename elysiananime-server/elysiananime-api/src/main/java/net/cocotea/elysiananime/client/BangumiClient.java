package net.cocotea.elysiananime.client;

import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.annotation.ForestClient;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;

import java.util.List;

/**
 * Bangumi客户端工具，接口来源：<a href="https://github.com/bangumi/api">Api</a>
 *
 * @author CoCoTea
 * @since v2.2.2
 */
@ForestClient
public interface BangumiClient {

    /**
     * 基础URL地址
     */
    String BASE_URL = "https://api.bgm.tv";

    /**
     * 每日放送
     */
    @Get(BASE_URL + "/calendar")
    List<JSONObject> calendar();

    /**
     * 获取条目
     *
     * @param subjectId 条目 ID
     */
    @Get(BASE_URL + "/v0/subjects/${subjectId}")
    JSONObject subjects(@Var("subjectId") String subjectId);

}
