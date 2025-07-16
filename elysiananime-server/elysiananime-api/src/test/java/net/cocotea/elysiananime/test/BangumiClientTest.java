package net.cocotea.elysiananime.test;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import net.cocotea.elysiananime.client.BangumiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Import(scanPackages = {"net.cocotea.elysiananime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class BangumiClientTest {

    private static final Logger log = LoggerFactory.getLogger(BangumiClientTest.class);

    @Inject
    BangumiClient bangumiClient;

    @Test
    public void calendarTest() {
        List<JSONObject> calendar = bangumiClient.calendar();
        log.info(JSONUtil.toJsonStr(calendar));
    }

    @Test
    public void subjectsTest() {
        JSONObject subjects = bangumiClient.subjects("504054");
        log.info(JSONUtil.toJsonStr(subjects));
    }

}
