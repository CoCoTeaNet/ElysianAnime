package net.cocotea.elysiananime.api.anime.media;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 扫描状态对象：运行中以当前实例为准，结束后整份 JSON 落 Redis，重启后仍能返回上一次结果。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Data
@Accessors(chain = true)
public class MediaScanStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = -2146723866828429628L;

    /**
     * 是否正在扫描
     */
    private boolean running;

    /**
     * 本轮开始时间
     */
    private String startedAt;

    /**
     * 本轮需要扫描的作品数
     */
    private int total;

    /**
     * 已完成作品数
     */
    private int done;

    /**
     * 失败作品数
     */
    private int failed;

    /**
     * 未匹配目录数
     */
    private int orphans;

    /**
     * 最近一次完成的扫描结果
     */
    private MediaScanResult lastResult;
}
