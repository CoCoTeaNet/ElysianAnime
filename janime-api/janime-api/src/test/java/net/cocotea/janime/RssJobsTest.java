package net.cocotea.janime;

import net.cocotea.janime.jobs.RssJobs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

@Import(scanPackages = {"net.cocotea.janime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class RssJobsTest {

    @Inject
    private RssJobs rssJobs;

    @Test
    public void scanBt() {
        rssJobs.scanBt();
    }

}
