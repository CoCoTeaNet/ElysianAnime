package net.cocotea.elysiananime.api.anime.rss.model;

import com.alibaba.fastjson.JSONObject;

/**
 * qb种子属性
 */
public class QbInfo {

    /**
     * 文件哈希值
     */
    private String hash;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 保存目录
     */
    private String savePath;
    /**
     * 已保存的路径
     */
    private String contentPath;

    public String getName() {
        return name;
    }

    public QbInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getSavePath() {
        return savePath;
    }

    public QbInfo setSavePath(String savePath) {
        this.savePath = savePath;
        return this;
    }

    public String getContentPath() {
        return contentPath;
    }

    public QbInfo setContentPath(String contentPath) {
        this.contentPath = contentPath;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public QbInfo setHash(String hash) {
        this.hash = hash;
        return this;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
