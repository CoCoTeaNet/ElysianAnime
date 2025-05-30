package net.cocotea.elysiananime.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.service.RedisService;
import net.cocotea.elysiananime.properties.QbittorrentProp;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * qbittorrent 接口工具类
 * <p></p>
 * <a href="https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-(qBittorrent-4.1)">qbittorrent api</a>
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Component
public class QbApiUtils {
    private static final Logger logger = LoggerFactory.getLogger(QbApiUtils.class);

    @Inject
    private QbittorrentProp qbittorrentProp;

    @Inject
    private RedisService redisService;

    /**
     * qb登录
     *
     * @return cookie
     */
    public String login() {
        String url = qbittorrentProp.getDomain().concat("/api/v2/auth/login");
        JSONObject params = new JSONObject()
                .fluentPut("username", qbittorrentProp.getUsername())
                .fluentPut("password", qbittorrentProp.getPassword());
        HttpResponse response = HttpRequest.post(url).form(params).timeout(30000).execute();
        logger.info("qbApi[login]cookie={}", response.header("set-cookie"));
        return response.header("set-cookie");
    }

    /**
     * 获取登录票据
     *
     * @return cookie
     */
    public String getCookie() {
        String baseMsg = "qbApi[getCookie]";
        String cookie = redisService.get(RedisKeyConst.QB_COOKIE);
        if (StrUtil.isBlank(cookie)) {
            cookie = login();
            redisService.saveByHours(RedisKeyConst.QB_COOKIE, cookie, 24);
        } else {
            // 测试是否cookie是否有效
            String url = qbittorrentProp.getDomain().concat("/api/v2/torrents/info?filter=seeding&sort=ratio");
            String body = HttpUtil.createGet(url).header("cookie", cookie).execute().body();
            logger.debug("{}测试是否cookie是否有效,body={}", baseMsg, body);
            if (qbittorrentProp.getForbidden().equals(body)) {
                logger.warn("{}票据失效，重新登录", baseMsg);
                cookie = login();
            }
        }
        String sid = cookie.split(";")[0];
        logger.info("{}sid={}", baseMsg, sid);
        return sid;
    }

    /**
     * 添加下载种子
     *
     * @param btUrl    种子链接
     * @param savePath 保存路径
     * @return 响应信息
     */
    public String addNewTorrent(String btUrl, String savePath, String rename) {
        String baseMsg = "qbApi[addNewTorrent]";
        String cookie = getCookie();
        String url = qbittorrentProp.getDomain().concat("/api/v2/torrents/add");

        JSONObject params = new JSONObject()
                .fluentPut("urls", btUrl)
                .fluentPut("savepath", savePath)
                .fluentPut("category", qbittorrentProp.getCategory())
                .fluentPut("rename", rename);

        logger.info(baseMsg.concat("cookie={}"), cookie);
        logger.info(baseMsg.concat("params={}"), params);

        HttpResponse response = HttpUtil.createPost(url)
                .header("cookie", cookie)
                .header("Content-Type", "multipart/form-data; boundary=---------------------------6688794727912")
                .header("Content-Length", "length")
                .form(params)
                .execute();
        return response.body();
    }

    /**
     * 获取种子下载列表
     *
     * @param filter 状态过滤
     * @return 响应信息
     */
    public JSONArray info(String filter) {
        String url = qbittorrentProp.getDomain()
                .concat("/api/v2/torrents/info?filter=" + filter + "&sort=ratio&category=")
                .concat(qbittorrentProp.getCategory());
        String body = HttpUtil.createGet(url).header("cookie", getCookie()).execute().body();
        logger.debug("qbApi[info]body={}", body);
        if (StrUtil.isNotBlank(body)) {
            return JSONObject.parseObject(body, JSONArray.class);
        } else {
            return new JSONArray();
        }
    }

    /**
     * 暂停种子下载
     *
     * @param hashes 哈希列表，多个用 | 隔离
     * @return 响应信息
     */
    public String pause(String hashes) {
        String url = qbittorrentProp.getDomain().concat("/api/v2/torrents/pause");
        return HttpUtil
                .createPost(url)
                .header("cookie", getCookie())
                .form(new JSONObject().fluentPut("hashes", hashes))
                .execute()
                .body();
    }

    /**
     * 删除下载记录，不删除文件
     *
     * @param hashes 哈希列表，多个用 | 隔离
     * @return 响应信息
     */
    public String delete(String hashes) {
        return delete(hashes, false);
    }

    /**
     * 删除下载记录
     *
     * @param hashes      哈希列表，多个用 | 隔离
     * @param deleteFiles true表示删除文件
     * @return 响应信息
     */
    public String delete(String hashes, boolean deleteFiles) {
        String url = qbittorrentProp.getDomain().concat("/api/v2/torrents/delete");
        return HttpUtil
                .createPost(url)
                .header("cookie", getCookie())
                .form(new JSONObject().fluentPut("hashes", hashes).fluentPut("deleteFiles", deleteFiles))
                .execute()
                .body();
    }

    /**
     * bt种子重命名
     *
     * @param hash 种子哈希
     * @param name 新名称
     * @return 响应信息
     */
    public String rename(String hash, String name) {
        String url = qbittorrentProp.getDomain().concat("/api/v2/torrents/rename");
        return HttpUtil
                .createPost(url)
                .header("cookie", getCookie())
                .form(new JSONObject().fluentPut("hash", hash).fluentPut("name", name))
                .execute()
                .body();
    }

    /**
     * 重命名文件
     *
     * @param hash    种子哈希
     * @param newPath 新路径
     * @param oldPath 旧路径
     * @return 响应信息
     */
    public String renameFile(String hash, String newPath, String oldPath) {
        String url = qbittorrentProp.getDomain().concat("/api/v2/torrents/renameFile");

        logger.info("renameFile >>>>> hash={}, newPath={}, oldPath={}", hash, newPath, oldPath);

        return HttpUtil.createPost(url)
                .header("cookie", getCookie())
                .form(new JSONObject().fluentPut("hash", hash).fluentPut("newPath", newPath).fluentPut("oldPath", oldPath))
                .execute()
                .body();
    }

}
