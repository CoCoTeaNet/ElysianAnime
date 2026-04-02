package net.cocotea.elysiananime.test;

import net.cocotea.elysiananime.util.SecurityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Import(scanPackages = {"net.cocotea.elysiananime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class PwdTest {

    private static final Logger log = LoggerFactory.getLogger(PwdTest.class);

    @Inject
    SecurityUtils securityUtils;

    @Test
    public void calendarTest() {
        String pwd = securityUtils.getPwd("123456");
        log.info(pwd);
    }

}
