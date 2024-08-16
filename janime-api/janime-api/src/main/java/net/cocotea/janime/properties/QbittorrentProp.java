package net.cocotea.janime.properties;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * Qbittorrent相关配置
 */
@Data
@Component
public class QbittorrentProp {

    @Inject("${janime.qb.ok}")
    private String ok = "Ok.";

    @Inject("${janime.qb.forbidden}")
    private String forbidden = "Forbidden";

    @Inject("${janime.qb.category}")
    private String category = "JAnime";

    @Inject("${janime.qb.domain}")
    private String domain;

    @Inject("${janime.qb.username}")
    private String username;

    @Inject("${janime.qb.password}")
    private String password;

}
