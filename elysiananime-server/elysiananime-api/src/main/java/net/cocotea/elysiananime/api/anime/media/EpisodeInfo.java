package net.cocotea.elysiananime.api.anime.media;

import java.math.BigDecimal;

/**
 * 单个媒体文件的集数解析结果
 *
 * @param episodeNo   正片集数；SP 与无集数为 null
 * @param episodeKey  逻辑集数键：1 / 12.5 / SP1 / X+文件名MD5前8位
 * @param displayName 列表展示名
 * @param sortNo      排序号：正片=集数，SP=10000+序号，无集数为 null
 * @author CoCoTea
 * @since 3.1.0
 */
public record EpisodeInfo(BigDecimal episodeNo, String episodeKey, String displayName, BigDecimal sortNo) {

    public boolean isExtra() {
        return episodeNo == null;
    }
}
