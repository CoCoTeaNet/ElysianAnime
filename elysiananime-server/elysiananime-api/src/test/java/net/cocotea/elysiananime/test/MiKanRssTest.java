package net.cocotea.elysiananime.test;

import lombok.extern.slf4j.Slf4j;
import net.cocotea.elysiananime.api.anime.model.dto.AniAddOpusTorrentDTO;
import net.cocotea.elysiananime.api.anime.rss.MiKanRss;
import net.cocotea.elysiananime.common.model.BusinessException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

@Slf4j
@Import(scanPackages = {"net.cocotea.elysiananime"})
@RunWith(SolonJUnit4ClassRunner.class)
public class MiKanRssTest {

    @Inject
    MiKanRss miKanRss;

    @Test
    public void addOpusTorrentTest() throws BusinessException {
        AniAddOpusTorrentDTO torrentDTO = new AniAddOpusTorrentDTO()
                .setOpusId("123123131")
                .setTorrentUrl("http://")
                .setEpisodes(13)
                .setFileType("mp4");
        String msg = miKanRss.addOpusTorrent(torrentDTO);
        log.info(msg);
    }

}
