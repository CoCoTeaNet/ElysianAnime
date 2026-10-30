package net.cocotea.elysiananime.test;

import net.cocotea.elysiananime.api.anime.model.vo.AniVideoVO;
import net.cocotea.elysiananime.api.anime.service.impl.AniOpusServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 资源目录扫描相关测试
 */
public class AniOpusServiceImplTest {

    /**
     * 资源目录不可读时 File.listFiles() 返回 null，修复前这里会抛 NPE，导致首页把新番显示成 0 集
     */
    @Test
    public void getMediasWithNullFilesTest() {
        Map<String, List<AniVideoVO.Media>> medias = new AniOpusServiceImpl().getMedias(null);
        assertTrue(medias.get("mediaList").isEmpty());
        assertTrue(medias.get("noSortMediaList").isEmpty());
    }
}
