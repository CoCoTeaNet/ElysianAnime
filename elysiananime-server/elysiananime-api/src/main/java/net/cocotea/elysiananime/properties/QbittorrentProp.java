package net.cocotea.elysiananime.properties;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * Qbittorrent相关配置
 */
@Data
@Component
public class QbittorrentProp {

    @Inject("${elysiananime.qb.ok}")
    private String ok = "Ok.";

    @Inject("${elysiananime.qb.forbidden}")
    private String forbidden = "Forbidden";

    @Inject("${elysiananime.qb.category}")
    private String category = "ElysianAnime";

    @Inject("${elysiananime.qb.domain}")
    private String domain;

    @Inject("${elysiananime.qb.username}")
    private String username;

    @Inject("${elysiananime.qb.password}")
    private String password;

}
