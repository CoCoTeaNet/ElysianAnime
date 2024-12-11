package net.cocotea.elysiananime.test;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import net.cocotea.elysiananime.api.anime.rss.model.QbInfo;
import net.cocotea.elysiananime.util.QbApiUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

import java.io.File;

@Import(scanPackages = {"net.cocotea.elysiananime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class QbApiUtilsTest {

    @Inject
    QbApiUtils qbApiUtils;

    @Test
    public void test() {
        String cookie = qbApiUtils.login();
        System.out.println(cookie);
    }

    @Test
    public void addNewTorrent() {
        String btUrl = "https://mikanime.tv/Download/20241208/ce47f85b4240a4f630f6eda5c257cad424287a1f.torrent";
        String savePath = "D:\\data\\qb_download";

        String msg = qbApiUtils.addNewTorrent(btUrl, savePath);
        System.out.println(msg);
    }

    @Test
    public void listTorrent() {
        JSONArray all = qbApiUtils.info("all");
        for (Object obj : all) {
            QbInfo qbInfo = BeanUtil.toBean(obj, QbInfo.class);
            System.out.println(qbInfo);
        }
    }

}
