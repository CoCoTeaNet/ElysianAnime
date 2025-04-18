package net.cocotea.elysiananime.api.anime.rss.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * qb种子属性
 */
@Accessors(chain = true)
@Data
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

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
