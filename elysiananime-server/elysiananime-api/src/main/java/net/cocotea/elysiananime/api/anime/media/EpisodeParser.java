package net.cocotea.elysiananime.api.anime.media;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 媒体文件名 → 集数键。
 * <p>
 * 规则按顺序命中（见 doc/plan/playback-media-rework.md §5.1）：
 * <ol>
 *   <li>额外内容（SP / OVA / 特别篇 / 特典 / PV）必须最先判断，否则 {@code SP01.mkv} 会被当成第 1 集</li>
 *   <li>{@code 第12话} / {@code 第12集} / {@code 第十二话}</li>
 *   <li>{@code [12]} / {@code 【12】} / {@code E12} / {@code EP12} / {@code - 12}</li>
 *   <li>小数集数 {@code 12.5}</li>
 *   <li>最后一个独立数字（排除 1080 / 2160 / 264 / 年份等）</li>
 *   <li>都没命中 → {@code X + 文件名MD5前8位}，不能兜底成 SP1，否则两个剧场版会撞键</li>
 * </ol>
 * 目录名只在文件位于作品目录的子目录里时传入（{@code 第01话/xxx.mkv}），
 * 作品目录名不参与解析——番名里的数字（如「86 -不存在的战区-」）会被误判成集数。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
public final class EpisodeParser {

    private static final BigDecimal SP_BASE = BigDecimal.valueOf(10000);

    private static final Pattern EXTRA = Pattern.compile("(?i)(?<![a-z])(sp|ova|pv|特别篇|特典)(?![a-z])");
    private static final Pattern EPISODE_MARK = Pattern.compile(
            "第\\s*([0-9]{1,4}(?:\\.[0-9]{1,2})?|[一二三四五六七八九十百零〇两]{1,4})\\s*[话話集]");
    private static final Pattern BRACKET = Pattern.compile("[\\[【]\\s*([0-9]{1,4}(?:\\.[0-9]{1,2})?)\\s*[\\]】]");
    private static final Pattern EP_PREFIX = Pattern.compile(
            "(?i)(?<![a-z])ep?\\s*[._-]?\\s*([0-9]{1,4}(?:\\.[0-9]{1,2})?)(?![0-9])");
    private static final Pattern DASH_SUFFIX = Pattern.compile(
            "[-–—_]\\s*([0-9]{1,4}(?:\\.[0-9]{1,2})?)\\s*(?=$|[\\[\\(（【])");
    private static final Pattern PLAIN_DECIMAL = Pattern.compile("^[0-9]{1,4}\\.[0-9]{1,2}$");
    private static final Pattern PLAIN_NUMBER = Pattern.compile("^[0-9]{1,4}(?:\\.[0-9]{1,2})?$");
    private static final Pattern NUMBER = Pattern.compile("(?<![0-9.])([0-9]{1,4}(?:\\.[0-9]{1,2})?)(?![0-9.])");

    /**
     * 只保留"几乎不可能是集数"的分辨率/编码数字；8/10/12 这类值本身可能是集数，
     * 由 {@link #isNoise} 里的 {@code 8bit / 10bit} 后缀判断兜住
     */
    private static final Set<Integer> NOISE_NUMBERS = Set.of(
            264, 265, 480, 576, 720, 1080, 1440, 2160, 4320);

    private EpisodeParser() {
    }

    /**
     * @param fileName 文件名（可含扩展名）
     * @param dirName  文件所在的一级目录名；文件直接在作品目录下时传 null
     */
    public static EpisodeInfo parse(String fileName, String dirName) {
        String baseName = stripExtension(fileName);
        if (containsExtra(dirName) || containsExtra(baseName)) {
            // 目录只标了「SP」而编号在文件名里（SP/01.mkv），两边都要找一遍
            BigDecimal no = pickNumber(dirName);
            if (no == null) {
                no = pickNumber(baseName);
            }
            if (no != null && no.stripTrailingZeros().scale() <= 0) {
                BigDecimal order = SP_BASE.add(no);
                String key = "SP" + no.toBigInteger();
                return new EpisodeInfo(null, key, key, order);
            }
            return fallback(baseName);
        }

        BigDecimal episodeNo = parseDirectory(dirName);
        if (episodeNo == null) {
            episodeNo = parseExplicit(baseName);
        }
        if (episodeNo == null) {
            return fallback(baseName);
        }
        String number = stripZeros(episodeNo);
        return new EpisodeInfo(episodeNo, number, "第 " + number + " 集", episodeNo);
    }

    /**
     * 去掉扩展名，只去掉最后一段（{@code [Group] 1.5.mkv} → {@code [Group] 1.5}）
     */
    public static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot <= 0 ? fileName : fileName.substring(0, dot);
    }

    /**
     * 集数键统一形式：{@code 01 → 1}、{@code 12.50 → 12.5}
     */
    public static String stripZeros(BigDecimal number) {
        BigDecimal stripped = number.stripTrailingZeros();
        return stripped.scale() <= 0 ? stripped.toBigInteger().toString() : stripped.toPlainString();
    }

    private static EpisodeInfo fallback(String baseName) {
        String key = "X" + MediaPathUtils.hashName(baseName);
        return new EpisodeInfo(null, key, baseName, null);
    }

    private static boolean containsExtra(String text) {
        return text != null && EXTRA.matcher(text).find();
    }

    /**
     * 目录名只在它自己就像集数目录时才参与解析（{@code 第01话} / {@code 01} / {@code E01}）。
     * 不用宽松规则：{@code Season 1} 里的 "1" 是季数，宽松匹配会把整季文件都算成第 1 集。
     */
    private static BigDecimal parseDirectory(String dirName) {
        if (dirName == null || dirName.isBlank()) {
            return null;
        }
        String text = dirName.trim();
        Matcher matcher = EPISODE_MARK.matcher(text);
        if (matcher.find()) {
            BigDecimal number = toNumber(matcher.group(1));
            if (number != null) {
                return number;
            }
        }
        matcher = BRACKET.matcher(text);
        while (matcher.find()) {
            if (!isNoise(text, matcher.start(1), matcher.end(1), matcher.group(1))) {
                return new BigDecimal(matcher.group(1));
            }
        }
        matcher = EP_PREFIX.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        if (PLAIN_NUMBER.matcher(text).matches()) {
            return new BigDecimal(text);
        }
        return null;
    }

    private static BigDecimal parseExplicit(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = EPISODE_MARK.matcher(text);
        if (matcher.find()) {
            BigDecimal number = toNumber(matcher.group(1));
            if (number != null) {
                return number;
            }
        }
        matcher = BRACKET.matcher(text);
        while (matcher.find()) {
            // [2024] 这类年份、[1080] 这类分辨率同样出现在方括号里
            if (!isNoise(text, matcher.start(1), matcher.end(1), matcher.group(1))) {
                return new BigDecimal(matcher.group(1));
            }
        }
        matcher = EP_PREFIX.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        matcher = DASH_SUFFIX.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        if (PLAIN_DECIMAL.matcher(text.trim()).matches()) {
            return new BigDecimal(text.trim());
        }
        return pickNumber(text);
    }

    /**
     * 取文本里最后一个"看起来像集数"的独立数字
     */
    private static BigDecimal pickNumber(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = NUMBER.matcher(text);
        BigDecimal candidate = null;
        while (matcher.find()) {
            if (!isNoise(text, matcher.start(1), matcher.end(1), matcher.group(1))) {
                candidate = new BigDecimal(matcher.group(1));
            }
        }
        return candidate;
    }

    private static boolean isNoise(String text, int start, int end, String token) {
        if (start > 0 && (text.charAt(start - 1) == 'x' || text.charAt(start - 1) == 'X')) {
            return true;
        }
        String tail = text.substring(end).toLowerCase(Locale.ROOT);
        if (tail.startsWith("p") || tail.startsWith("bit") || tail.startsWith("fps") || tail.startsWith("hz")) {
            return true;
        }
        BigDecimal value = new BigDecimal(token);
        if (value.stripTrailingZeros().scale() > 0) {
            return false;
        }
        int number = value.intValue();
        if (NOISE_NUMBERS.contains(number)) {
            return true;
        }
        return number >= 1900 && number <= 2100;
    }

    private static BigDecimal toNumber(String token) {
        if (token.matches("[0-9.]+")) {
            return new BigDecimal(token);
        }
        return chineseToNumber(token);
    }

    /**
     * 支持一 ~ 九百九十九，够覆盖集数
     */
    private static BigDecimal chineseToNumber(String text) {
        int total = 0;
        int current = 0;
        for (char c : text.toCharArray()) {
            switch (c) {
                case '零', '〇' -> current = 0;
                case '一' -> current = 1;
                case '二', '两' -> current = 2;
                case '三' -> current = 3;
                case '四' -> current = 4;
                case '五' -> current = 5;
                case '六' -> current = 6;
                case '七' -> current = 7;
                case '八' -> current = 8;
                case '九' -> current = 9;
                case '十' -> {
                    total += (current == 0 ? 1 : current) * 10;
                    current = 0;
                }
                case '百' -> {
                    total += (current == 0 ? 1 : current) * 100;
                    current = 0;
                }
                default -> {
                    return null;
                }
            }
        }
        return BigDecimal.valueOf(total + current);
    }
}
