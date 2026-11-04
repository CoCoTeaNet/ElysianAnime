package net.cocotea.elysiananime.api.anime.media;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * 媒体路径归一化与哈希。
 * <p>
 * 同一个文件可能以 {@code D:/a/b.mkv}、{@code D:\a\b.mkv}、{@code D:\a\..\a\b.mkv}
 * 等不同写法出现，归一化后再取 MD5 才能让唯一键 {@code (opus_id, file_path_hash)}
 * 真正生效。归一化顺序见 {@code doc/plan/playback-media-rework.md} §5.6：
 * {@code toRealPath} → 分隔符统一为 {@code /} → Windows 下转小写。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
public final class MediaPathUtils {

    private static final boolean WINDOWS = File.separatorChar == '\\';

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private MediaPathUtils() {
    }

    /**
     * 归一化为哈希/比较用的形式：真实路径、{@code /} 分隔、Windows 下小写。
     * <p>
     * 路径不存在时退化为绝对路径 + {@code normalize()}，扫描期间文件刚被移走也能算出一致的键。
     */
    public static String normalize(String rawPath) {
        return normalizeReal(realPath(rawPath));
    }

    /**
     * 对已经是真实路径的字符串做归一化（只统一分隔符与大小写，不再解析一遍文件系统）。
     * <p>
     * 扫描时每个文件都已经解析过一次真实路径，再走一次 {@link #normalize} 就是多一次
     * {@code toRealPath()} 系统调用，1 万个文件时这部分开销不小。
     */
    public static String normalizeReal(String realPath) {
        if (realPath == null || realPath.isBlank()) {
            return "";
        }
        String unified = realPath.replace('\\', '/');
        return WINDOWS ? unified.toLowerCase(Locale.ROOT) : unified;
    }

    /**
     * 归一化为入库 {@code file_path} 的形式：真实路径、{@code /} 分隔，保留磁盘上的大小写，
     * 供读取文件时直接使用（Windows 上大小写不敏感，Linux 上必须与实际一致）。
     */
    public static String realPath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "";
        }
        Path path = Paths.get(rawPath);
        try {
            path = path.toRealPath();
        } catch (IOException ignored) {
            path = path.toAbsolutePath().normalize();
        }
        return path.toString().replace('\\', '/');
    }

    /**
     * 归一化路径的 MD5，写入 {@code ani_media.file_path_hash}
     */
    public static String hashPath(String rawPath) {
        return md5(normalize(rawPath));
    }

    /**
     * 文件名（去扩展名）的 MD5 前 8 位，用于无集数媒体的 {@code X+hash8} 键
     */
    public static String hashName(String name) {
        return md5(name).substring(0, 8);
    }

    /**
     * 判断文件是否位于根目录内，流式播放与扫描都靠它挡住路径穿越
     *
     * @param normalizedRoot 已归一化的根目录
     * @param normalizedPath 已归一化的文件路径
     */
    public static boolean isUnder(String normalizedRoot, String normalizedPath) {
        String root = trimTrailingSlash(normalizedRoot);
        String path = trimTrailingSlash(normalizedPath);
        if (root.isEmpty() || path.isEmpty()) {
            return false;
        }
        return path.equals(root) || path.startsWith(root + "/");
    }

    /**
     * 相对资源根的路径，用于展示（{@code relative_path}）：保留磁盘上的大小写
     * <p>
     * 传入的是真实路径（{@link #realPath}）而不是归一化路径——归一化在 Windows 下会转小写，
     * 展示用的路径不该把 {@code [Group] Ep01.mkv} 变成 {@code [group] ep01.mkv}。
     *
     * @param root 资源根路径
     * @param path 文件路径
     */
    public static String relativeTo(String root, String path) {
        String trimmedRoot = trimTrailingSlash(root);
        String trimmedPath = trimTrailingSlash(path);
        if (!underRoot(trimmedRoot, trimmedPath)) {
            return trimmedPath;
        }
        return trimmedPath.length() == trimmedRoot.length() ? "" : trimmedPath.substring(trimmedRoot.length() + 1);
    }

    /**
     * 根目录包含判断；Windows 下大小写不敏感
     */
    private static boolean underRoot(String root, String path) {
        if (root.isEmpty() || path.isEmpty()) {
            return false;
        }
        if (path.equals(root)) {
            return true;
        }
        if (path.length() <= root.length()) {
            return false;
        }
        if (WINDOWS) {
            return path.regionMatches(true, 0, root, 0, root.length())
                    && path.charAt(root.length()) == '/';
        }
        return path.startsWith(root + "/");
    }

    /**
     * 小写十六进制 MD5
     */
    public static String md5(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            char[] chars = new char[bytes.length * 2];
            for (int i = 0; i < bytes.length; i++) {
                int value = bytes[i] & 0xff;
                chars[i * 2] = HEX[value >>> 4];
                chars[i * 2 + 1] = HEX[value & 0x0f];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 不可用", e);
        }
    }

    private static String trimTrailingSlash(String path) {
        if (path == null) {
            return "";
        }
        int end = path.length();
        while (end > 1 && path.charAt(end - 1) == '/') {
            end--;
        }
        return path.substring(0, end);
    }
}
