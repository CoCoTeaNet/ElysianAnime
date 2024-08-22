package net.cocotea.janime;

import net.cocotea.janime.util.QbApiUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

@Import(scanPackages = {"net.cocotea.janime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class QbApiUtilsTest {

    @Inject
    private QbApiUtils qbApiUtils;

    @Test
    public void test() {
        String cookie = qbApiUtils.login();
        System.out.println(cookie);
    }

}
