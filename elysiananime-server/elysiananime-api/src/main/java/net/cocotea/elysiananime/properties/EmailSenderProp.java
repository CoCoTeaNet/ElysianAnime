package net.cocotea.elysiananime.properties;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

@Component
@Data
public class EmailSenderProp {

    @Inject("${elysiananime.emailsender.user}")
    private String user;

    @Inject("${elysiananime.emailsender.from}")
    private String from;

    @Inject("${elysiananime.emailsender.pass}")
    private String pass;

    @Inject("${elysiananime.emailsender.host}")
    private String host;

    @Inject("${elysiananime.emailsender.port}")
    private Integer port;

}
