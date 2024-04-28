package net.cocotea.janime.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Qbittorrent相关配置
 */
@Data
@Component
public class QbittorrentProp {

    @Value("${janime.qb.ok}")
    private String ok = "Ok.";

    @Value("${janime.qb.forbidden}")
    private String forbidden = "Forbidden";

    @Value("${janime.qb.category}")
    private String category = "JAnime";

    @Value("${janime.qb.domain}")
    private String domain;

    @Value("${janime.qb.username}")
    private String username;

    @Value("${janime.qb.password}")
    private String password;

}
