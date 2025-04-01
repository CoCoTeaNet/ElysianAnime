package net.cocotea.elysiananime.test;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson.JSONArray;
import net.cocotea.elysiananime.api.anime.rss.model.QbInfo;
import net.cocotea.elysiananime.util.QbApiUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

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
        String added = qbApiUtils.addNewTorrent(
                "https://mikanime.tv/Download/20241130/223fa07481409f6bf076484c8104c8a8ba330c2d.torrent",
                "D:\\test",
                "09.mkv"
        );
        System.out.println(added);
    }

    @Test
    public void info() {
        JSONArray list = qbApiUtils.info("all");
        for (Object obj : list) {
            System.out.println(BeanUtil.toBean(obj, QbInfo.class));
        }
    }

    @Test
    public void renameFile() {
        JSONArray list = qbApiUtils.info("all");
        for (Object obj : list) {
            String fileSeparator = SystemUtil.get(SystemUtil.FILE_SEPARATOR);

            QbInfo qbInfo = BeanUtil.toBean(obj, QbInfo.class);
            String relativePath = StrUtil.replace(qbInfo.getContentPath(), qbInfo.getSavePath()+fileSeparator, "");

            System.out.println("系统文件分隔符：" + fileSeparator);
            System.out.println(relativePath);

            String renamed = qbApiUtils.renameFile(qbInfo.getHash(), "example-03.mp4", relativePath);
            System.out.println(renamed);
        }
    }

}
