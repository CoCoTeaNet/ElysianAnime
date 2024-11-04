package net.cocotea.elysiananime.test;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.json.JSONUtil;
import net.cocotea.elysiananime.properties.EmailSenderProp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Import(scanPackages = {"net.cocotea.elysiananime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class EmailSendTest {

    private static final Logger log = LoggerFactory.getLogger(EmailSendTest.class);

    @Inject
    EmailSenderProp emailSenderProp;

    @Test
    public void send() {
        String emailHtml = "    <div>" +
                "        <p>中文名：测试</p>" +
                "        <p>原名：测试2233</p>" +
                "        <p>资源名称：none</p>" +
                "    </div>";
        String emailTitle = "ElysianAnime：你追的番剧更新了~~~【测试】";
        MailAccount mailAccount = new MailAccount()
                .setUser(emailSenderProp.getUser())
                .setFrom(emailSenderProp.getFrom())
                .setPass(emailSenderProp.getPass())
                .setHost(emailSenderProp.getHost())
                .setPort(emailSenderProp.getPort())
                .setSslEnable(emailSenderProp.getSslEnable());
        log.info(JSONUtil.toJsonStr(emailSenderProp));
        // MailUtil.send(mailAccount, "2233@qq.com", emailTitle, emailHtml, true);
    }

}
