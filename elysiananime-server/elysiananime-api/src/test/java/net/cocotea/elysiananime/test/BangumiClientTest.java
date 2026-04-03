package net.cocotea.elysiananime.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.cocotea.elysiananime.api.anime.service.AniSpiderService;
import net.cocotea.elysiananime.client.BangumiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Import(scanPackages = {"net.cocotea.elysiananime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class BangumiClientTest {

    private static final Logger log = LoggerFactory.getLogger(BangumiClientTest.class);

    @Inject
    BangumiClient bangumiClient;

    @Inject
    AniSpiderService aniSpiderService;

    @Test
    public void calendarTest() {
        JSONArray calendar = bangumiClient.calendar();
        log.info(JSON.toJSONString(calendar));
    }

    @Test
    public void subjectsTest() {
        JSONObject subjects = bangumiClient.subjects("504054");
        log.info(JSON.toJSONString(subjects));
    }

    @Test
    public void addOpusFromBangumiTest() throws Exception {
        JSONObject object = aniSpiderService.fetchOpusFromBangumi("504054");
        log.info(JSON.toJSONString(object));
    }

}
