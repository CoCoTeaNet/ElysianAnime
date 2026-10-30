package net.cocotea.elysiananime.api.anime.media;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import net.cocotea.elysiananime.api.anime.model.po.AniMedia;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.properties.FileProp;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 媒体流输出：支持 Range / 206 / HEAD / 416。
 * <p>
 * 状态码全部自己写，不抛异常——`AppFilter` 会把异常渲染成 HTTP 200 + JSON，
 * `&lt;video&gt;` 收到 200 的一段 JSON 只会表现为「解码失败」，而不是「登录失效」。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Component
public class MediaStreamer {

    private static final Logger log = LoggerFactory.getLogger(MediaStreamer.class);

    @Inject
    private FileProp fileProp;

    /**
     * 输出媒体流
     *
     * @param media 媒体索引行
     * @param ctx   上下文
     */
    public void stream(AniMedia media, Context ctx) throws IOException {
        try {
            doStream(media, ctx);
        } finally {
            markHandled(ctx);
        }
    }

    private void doStream(AniMedia media, Context ctx) throws IOException {
        if (media == null) {
            writeError(ctx, 404, "资源不存在");
            return;
        }
        java.nio.file.Path path = resolveMediaPath(media.getFilePath());
        if (path == null) {
            log.warn("媒体路径不在资源根目录内，拒绝访问 mediaId={} path={}", media.getId(), media.getFilePath());
            writeError(ctx, 404, "资源不存在");
            return;
        }
        long total;
        try {
            total = Files.size(path);
        } catch (IOException e) {
            log.warn("媒体文件不可读 mediaId={} path={}", media.getId(), path);
            writeError(ctx, 500, "读取媒体失败");
            return;
        }
        if (total <= 0) {
            writeError(ctx, 404, "资源不存在");
            return;
        }

        Range range;
        try {
            range = parseRange(ctx.header("Range"), total);
        } catch (InvalidRangeException e) {
            ctx.status(416);
            ctx.headerSet("Accept-Ranges", "bytes");
            ctx.headerSet("Content-Range", "bytes */" + total);
            ctx.contentType("text/plain;charset=UTF-8");
            // 必须真正写一次：Solon 只在取输出流时才把状态码刷到响应上，
            // 只 set 状态不写内容的话，客户端收到的是 200 而不是 416
            ctx.output("");
            ctx.close();
            return;
        }
        boolean partial = range != null;
        long start = partial ? range.start : 0;
        long length = partial ? range.end - range.start + 1 : total;

        ctx.status(partial ? 206 : 200);
        ctx.headerSet("Accept-Ranges", "bytes");
        ctx.contentType(contentType(media.getExtension()));
        ctx.contentLength(length);
        ctx.headerSet("Content-Disposition", contentDisposition(media.getFileName()));
        if (partial) {
            ctx.headerSet("Content-Range", "bytes " + range.start + "-" + range.end + "/" + total);
        }
        if ("HEAD".equalsIgnoreCase(ctx.method())) {
            // 同上：取一次输出流触发状态码下发，再把 Content-Length 补回真实长度
            ctx.outputStream();
            ctx.contentLength(length);
            ctx.close();
            return;
        }
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r");
             InputStream input = new BoundedInputStream(Channels.newInputStream(file.getChannel()), length)) {
            file.seek(start);
            ctx.output(input);
        } catch (Exception e) {
            log.error("媒体流输出失败 mediaId={}", media.getId(), e);
            if (!ctx.isHeadersSent()) {
                ctx.status(500);
            }
        }
    }

    /**
     * 路径必须在配置的资源根目录内，且是普通文件
     */
    private java.nio.file.Path resolveMediaPath(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            return null;
        }
        String normalized = MediaPathUtils.normalize(filePath);
        String rawRoots = fileProp.getAnimationSavePath();
        if (StrUtil.isBlank(rawRoots)) {
            return null;
        }
        for (String rootText : rawRoots.split(",")) {
            if (StrUtil.isBlank(rootText)) {
                continue;
            }
            if (MediaPathUtils.isUnder(MediaPathUtils.normalize(rootText.trim()), normalized)) {
                java.nio.file.Path path = Paths.get(filePath);
                return Files.isRegularFile(path) ? path : null;
            }
        }
        return null;
    }

    /**
     * 直接写错误状态码与 JSON，不抛异常
     */
    public void writeError(Context ctx, int status, String message) throws IOException {
        ctx.status(status);
        ctx.contentType("application/json;charset=utf-8");
        ctx.output(JSON.toJSONString(new ApiResult<>(status, null, message)));
        ctx.close();
        markHandled(ctx);
    }

    /**
     * 响应已由本类直接写出：标记为已处理，
     * 避免框架再渲染一次返回值（输出流已关闭，会抛 writeBuffer has closed），
     * 也避免 AppFilter 把状态改成 404。
     */
    private void markHandled(Context ctx) {
        ctx.setRendered(true);
        ctx.setHandled(true);
    }

    private static String contentType(String extension) {
        return switch (extension == null ? "" : extension.toLowerCase()) {
            case "mp4" -> "video/mp4";
            case "mkv" -> "video/x-matroska";
            case "webm" -> "video/webm";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            case "flv" -> "video/x-flv";
            case "wmv" -> "video/x-ms-wmv";
            case "mpg", "mpeg" -> "video/mpeg";
            case "swf" -> "application/x-shockwave-flash";
            default -> "application/octet-stream";
        };
    }

    /**
     * 中文文件名按 RFC 5987 编码，{@code filename=} 里只保留 ASCII 兜底
     */
    private static String contentDisposition(String fileName) {
        String name = StrUtil.isBlank(fileName) ? "video" : fileName;
        String fallback = name.replaceAll("[^\\x20-\\x7e]", "_")
                .replace("\"", "_")
                .replace("\\", "_");
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        return "inline; filename=\"" + fallback + "\"; filename*=UTF-8''" + encoded;
    }

    /**
     * @return null 表示按整段响应（无 Range 或多段 Range）
     */
    private static Range parseRange(String header, long total) throws InvalidRangeException {
        if (header == null || !header.startsWith("bytes=")) {
            return null;
        }
        String spec = header.substring("bytes=".length()).trim();
        if (spec.contains(",")) {
            return null;
        }
        int dash = spec.indexOf('-');
        if (dash < 0) {
            throw new InvalidRangeException();
        }
        String startText = spec.substring(0, dash).trim();
        String endText = spec.substring(dash + 1).trim();
        try {
            if (startText.isEmpty()) {
                long suffix = Long.parseLong(endText);
                if (suffix <= 0) {
                    throw new InvalidRangeException();
                }
                return new Range(Math.max(total - suffix, 0), total - 1);
            }
            long start = Long.parseLong(startText);
            long end = endText.isEmpty() ? total - 1 : Long.parseLong(endText);
            if (start > end || start >= total) {
                throw new InvalidRangeException();
            }
            return new Range(start, Math.min(end, total - 1));
        } catch (NumberFormatException e) {
            throw new InvalidRangeException();
        }
    }

    private record Range(long start, long end) {
    }

    private static class InvalidRangeException extends Exception {
    }

    /**
     * 只让底层流读 {@code limit} 字节，避免整段读入内存
     */
    private static class BoundedInputStream extends InputStream {
        private final InputStream delegate;
        private long remaining;

        BoundedInputStream(InputStream delegate, long limit) {
            this.delegate = delegate;
            this.remaining = limit;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int value = delegate.read();
            if (value >= 0) {
                remaining--;
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int read = delegate.read(buffer, offset, (int) Math.min(length, remaining));
            if (read > 0) {
                remaining -= read;
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
