package net.cocotea.elysiananime.api.anime.media;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 路径归一化：同一文件不同写法必须落到同一个哈希（§5.6）
 */
class MediaPathUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void sameFileDifferentSpellingsShareHash() throws IOException {
        Path file = Files.createFile(tempDir.resolve("episode.mkv"));
        Files.createDirectories(tempDir.resolve("sub"));
        String canonical = tempDir.resolve("episode.mkv").toString();
        String withDot = tempDir.resolve(".").resolve("episode.mkv").toString();
        String withParent = tempDir.resolve("sub").resolve("..").resolve("episode.mkv").toString();

        String expected = MediaPathUtils.hashPath(canonical);
        assertEquals(expected, MediaPathUtils.hashPath(withDot));
        assertEquals(expected, MediaPathUtils.hashPath(withParent));
        assertEquals(expected, MediaPathUtils.hashPath(file.toString()));
    }

    @Test
    void windowsSeparatorsCollapseToSlash() {
        String realPath = MediaPathUtils.realPath(tempDir.toString());
        assertFalse(realPath.contains("\\"));
        assertEquals(realPath, MediaPathUtils.realPath(realPath));
        String normalized = MediaPathUtils.normalize(tempDir.toString());
        assertEquals(File.separatorChar == '\\' ? realPath.toLowerCase() : realPath, normalized);
    }

    @Test
    void windowsPathIsCaseInsensitive() throws IOException {
        Assumptions.assumeTrue(File.separatorChar == '\\', "仅 Windows 需要大小写归一");
        Path file = Files.createFile(tempDir.resolve("CaseName.MKV"));
        assertEquals(MediaPathUtils.hashPath(file.toString()),
                MediaPathUtils.hashPath(file.toString().toLowerCase()));
        assertEquals(MediaPathUtils.normalize(file.toString()),
                MediaPathUtils.normalize(file.toString().toLowerCase()));
    }

    @Test
    void normalizeRejectsPathTraversalOutsideRoot() {
        String root = MediaPathUtils.normalize(tempDir.toString());
        String inside = MediaPathUtils.normalize(tempDir.resolve("opus").resolve("1.mkv").toString());
        String outside = MediaPathUtils.normalize(tempDir.resolve("..").resolve("escape.mkv").toString());

        assertTrue(MediaPathUtils.isUnder(root, inside));
        assertFalse(MediaPathUtils.isUnder(root, outside));
        assertFalse(MediaPathUtils.isUnder(root, MediaPathUtils.normalize(root + "/../escape.mkv")));
        assertFalse(MediaPathUtils.isUnder(root, ""));
    }

    @Test
    void siblingDirectoryWithCommonPrefixIsNotUnderRoot() {
        String root = MediaPathUtils.normalize(tempDir.resolve("media").toString());
        String sibling = MediaPathUtils.normalize(tempDir.resolve("media2").resolve("1.mkv").toString());
        assertFalse(MediaPathUtils.isUnder(root, sibling));
    }

    @Test
    void relativePathKeepsSeparatorsUnified() {
        String root = MediaPathUtils.normalize(tempDir.toString());
        String file = MediaPathUtils.normalize(tempDir.resolve("opus").resolve("1.mkv").toString());
        assertEquals("opus/1.mkv", MediaPathUtils.relativeTo(root, file));
    }

    @Test
    void relativePathKeepsOriginalCase() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("Group"));
        Path file = Files.createFile(dir.resolve("Ep01.mkv"));
        String root = MediaPathUtils.realPath(tempDir.toString());
        String target = MediaPathUtils.realPath(file.toString());
        assertEquals("Group/Ep01.mkv", MediaPathUtils.relativeTo(root, target));
    }

    @Test
    void hashNameIsEightHexChars() {
        String hash = MediaPathUtils.hashName("movie");
        assertEquals(8, hash.length());
        assertTrue(hash.matches("[0-9a-f]{8}"));
        assertEquals(hash, MediaPathUtils.hashName("movie"));
        assertFalse(hash.equals(MediaPathUtils.hashName("movie2")));
    }
}
