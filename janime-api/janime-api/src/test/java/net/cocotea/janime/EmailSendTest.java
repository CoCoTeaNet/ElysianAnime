package net.cocotea.janime;

import cn.hutool.extra.mail.MailUtil;
import org.junit.Test;

public class EmailSendTest {

    @Test
    public void send() {
        String emailHtml = "    <div>" +
                "        <p>中文名：测试</p>" +
                "        <p>原名：测试2233</p>" +
                "        <p>资源名称：none</p>" +
                "    </div>";
        String emailTitle = "ElysianAnime：你追的番剧更新了~~~【测试】";
        MailUtil.send("572315466@qq.com", emailTitle, emailHtml, true);
    }

}
