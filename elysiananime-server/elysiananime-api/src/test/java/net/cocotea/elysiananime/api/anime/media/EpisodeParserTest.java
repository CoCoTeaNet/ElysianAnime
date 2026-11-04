package net.cocotea.elysiananime.api.anime.media;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 集数解析规则（doc/plan/playback-media-rework.md §5.1）
 */
class EpisodeParserTest {

    @Test
    void plainNumber() {
        EpisodeInfo info = EpisodeParser.parse("12.mkv", null);
        assertEquals("12", info.episodeKey());
        assertEquals("第 12 集", info.displayName());
        assertEquals(0, info.episodeNo().compareTo(new java.math.BigDecimal("12")));
        assertEquals(0, info.sortNo().compareTo(new java.math.BigDecimal("12")));
        assertFalse(info.isExtra());
    }

    @Test
    void stripsLeadingZeros() {
        assertEquals("1", EpisodeParser.parse("01.mkv", null).episodeKey());
        assertEquals("1", EpisodeParser.parse("第01话/xxx.mkv", "第01话").episodeKey());
    }

    @Test
    void chineseNumeral() {
        EpisodeInfo info = EpisodeParser.parse("第十二话.mkv", null);
        assertEquals("12", info.episodeKey());
        assertFalse(info.isExtra());
    }

    @Test
    void spIsExtraAndNeverEpisodeOne() {
        EpisodeInfo info = EpisodeParser.parse("SP01.mkv", null);
        assertTrue(info.isExtra());
        assertNull(info.episodeNo());
        assertEquals("SP1", info.episodeKey());
        assertEquals("SP1", info.displayName());
        assertEquals(0, info.sortNo().compareTo(new java.math.BigDecimal("10001")));
    }

    @Test
    void spVariantsShareSameKey() {
        assertEquals(EpisodeParser.parse("SP01.mkv", null).episodeKey(),
                EpisodeParser.parse("SP 1.mkv", null).episodeKey());
        assertEquals("SP2", EpisodeParser.parse("特典 SP02.mkv", null).episodeKey());
        assertEquals("SP3", EpisodeParser.parse("OVA 3.mkv", null).episodeKey());
    }

    @Test
    void extraDirectoryWins() {
        // 目录已经标明是特典，文件名里的数字不能当正片集数
        EpisodeInfo info = EpisodeParser.parse("01.mkv", "SP");
        assertTrue(info.isExtra());
        assertEquals("SP1", info.episodeKey());
    }

    @Test
    void decimalEpisode() {
        assertEquals("12.5", EpisodeParser.parse("12.5.mkv", null).episodeKey());
        assertEquals("12.5", EpisodeParser.parse("[Group] 12.5 [1080p].mkv", null).episodeKey());
        assertEquals("1.5", EpisodeParser.parse("第1.5话.mkv", null).episodeKey());
    }

    @Test
    void bracketAndPrefixForms() {
        assertEquals("12", EpisodeParser.parse("[Group][12][1080p].mkv", null).episodeKey());
        assertEquals("12", EpisodeParser.parse("【12】.mkv", null).episodeKey());
        assertEquals("12", EpisodeParser.parse("E12.mkv", null).episodeKey());
        assertEquals("12", EpisodeParser.parse("[Group] EP12.mkv", null).episodeKey());
        assertEquals("5", EpisodeParser.parse("[Group] - 05 [1080p].mkv", null).episodeKey());
    }

    @Test
    void ignoresResolutionAndYear() {
        assertEquals("13", EpisodeParser.parse("[Group] 13 [1080p][2024].mkv", null).episodeKey());
        assertEquals("3", EpisodeParser.parse("[Group] 1080p 3.mkv", null).episodeKey());
    }

    @Test
    void unnumberedExtraDoesNotShareSp1() {
        EpisodeInfo first = EpisodeParser.parse("特典 预告.mkv", null);
        EpisodeInfo second = EpisodeParser.parse("特典 花絮.mkv", null);
        assertTrue(first.isExtra());
        assertNotNull(first.episodeKey());
        assertTrue(first.episodeKey().startsWith("X"));
        assertFalse(first.episodeKey().equals(second.episodeKey()));
        assertNull(first.sortNo());
    }

    @Test
    void fallbackIsXPlusHash8() {
        EpisodeInfo info = EpisodeParser.parse("movie.mkv", null);
        assertTrue(info.isExtra());
        assertEquals("X" + MediaPathUtils.hashName("movie"), info.episodeKey());
        assertEquals("movie", info.displayName());
        assertNull(info.episodeNo());
        assertNull(info.sortNo());
    }

    @Test
    void directoryFormOnlyWhenItLooksLikeEpisodeFolder() {
        assertEquals("1", EpisodeParser.parse("xxx.mkv", "第01话").episodeKey());
        assertEquals("12.5", EpisodeParser.parse("xxx.mkv", "12.5").episodeKey());
        // Season 1 的 1 是季数，不能把整季文件都算成第 1 集
        assertEquals("5", EpisodeParser.parse("05.mkv", "Season 1").episodeKey());
    }

    @Test
    void stripExtensionOnlyLastSegment() {
        assertEquals("[Group] 1.5", EpisodeParser.stripExtension("[Group] 1.5.mkv"));
        assertEquals("noext", EpisodeParser.stripExtension("noext"));
        assertEquals(".hidden", EpisodeParser.stripExtension(".hidden"));
    }
}
