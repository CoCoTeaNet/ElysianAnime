package net.cocotea.elysiananime.api.anime.media;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 一次扫描的结果概要，供前端「扫描状态」展示。
 * <p>
 * 字段全部用字符串/基本类型，直接 JSON 落 Redis，不涉及时间类型转换。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Data
@Accessors(chain = true)
public class MediaScanResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 5136877207732351019L;

    /**
     * 开始时间 yyyy-MM-dd HH:mm:ss
     */
    private String startedAt;

    /**
     * 结束时间 yyyy-MM-dd HH:mm:ss
     */
    private String finishedAt;

    /**
     * 耗时（毫秒）
     */
    private long durationMs;

    /**
     * 遍历到的作品目录数
     */
    private int opusTotal;

    /**
     * 新增媒体数
     */
    private int added;

    /**
     * 变化的媒体数（大小/修改时间/命名变化）
     */
    private int updated;

    /**
     * 软删除媒体数
     */
    private int removed;

    /**
     * 复活媒体数（软删后文件又出现）
     */
    private int revived;

    /**
     * 识别为重命名而复用的行数
     */
    private int renamed;

    /**
     * 跳过文件数（临时文件、隐藏文件、正在写入）
     */
    private int skipped;

    /**
     * 未能匹配到作品目录数
     */
    private int orphans;

    /**
     * 同一作品在多个根目录重复出现
     */
    private int conflicts;

    /**
     * 失败的作品数（目录不可读等，失败时不做软删）
     */
    private int failed;

    /**
     * 写入更新动态条数
     */
    private int updateRecords;

    /**
     * 说明信息（失败原因等）
     */
    private String message;
}
