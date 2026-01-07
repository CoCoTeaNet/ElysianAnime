package net.cocotea.elysiananime.test;

import net.cocotea.elysiananime.jobs.OpusJobs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

@Import(scanPackages = {"net.cocotea.elysiananime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class OpusJobsTest {

    @Inject
    OpusJobs opusJobs;

    // @Test
    // public void scanBt() {
    //     rssJobs.scanBt();
    // }

    @Test
    public void scanRss() {
        opusJobs.scanRss();
    }

    @Test
    public void autoDiscoverResource() {
        opusJobs.autoDiscoverResource();
    }

}
