package net.cocotea.elysiananime.test;

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

}
