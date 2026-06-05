package net.cocotea.elysiananime.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.cocotea.elysiananime.properties.ProxyProp;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.io.File;

/**
 * Bangumi客户端工具，接口来源：<a href="https://github.com/bangumi/api">Api</a>
 *
 * @author CoCoTea
 * @since v2.2.2
 */
@Component
public class BangumiClient {

    @Inject
    private ProxyProp proxyProp;

    /**
     * 基础URL地址
     */
    final String BASE_URL = "https://api.bgm.tv";

    /**
     * 每日放送
     */
    public JSONArray calendar() {
        String url = BASE_URL + "/calendar";
        String body;
        if ("true".equals(proxyProp.getEnable())) {
            body = HttpUtil.createGet(url).setProxy(proxyProp.getProxy()).execute().body();
        } else {
            body = HttpUtil.get(url);
        }
        return JSON.parseArray(body);
    }

    /**
     * 获取条目
     *
     * @param subjectId 条目 ID
     */
    public JSONObject subjects(String subjectId) {
        String url = BASE_URL + "/v0/subjects/" + subjectId;
        String body;
        if ("true".equals(proxyProp.getEnable())) {
            body = HttpUtil.createGet(url).setProxy(proxyProp.getProxy()).execute().body();
        } else {
            body = HttpUtil.get(url);
        }
        return JSON.parseObject(body);
    }

    public void downloadFile(String fileUrl, File saveDir) {
        if ("true".equals(proxyProp.getEnable())) {
            downloadProxy(fileUrl, saveDir);
        } else {
            HttpUtil.downloadFile(fileUrl, saveDir);
        }
    }

    private void downloadProxy(String url, File saveDir) {
        Assert.notBlank(url, "[url] is blank !");
        HttpRequest request = HttpUtil.createGet(url, true).setProxy(proxyProp.getProxy());
        HttpResponse response = request.executeAsync();
        if (response.isOk()) {
            response.writeBody(saveDir);
            response.close();
        } else {
            throw new HttpException("Server response error with status code: [{}]", response.getStatus());
        }
    }

}
