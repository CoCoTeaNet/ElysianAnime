package net.cocotea.elysiananime.api.anime.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import net.cocotea.elysiananime.api.anime.media.MediaScanStatus;
import net.cocotea.elysiananime.api.anime.media.MediaScanner;
import net.cocotea.elysiananime.api.anime.media.MediaStreamer;
import net.cocotea.elysiananime.api.anime.model.po.AniMedia;
import net.cocotea.elysiananime.common.model.ApiResult;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

/**
 * 媒体接口：扫描与流式播放。
 * <p>
 * 播放接口不能依赖拦截器：`AppFilter` 会把所有异常渲染成 HTTP 200 + JSON，
 * `&lt;video&gt;` 收到 200 的一段 JSON 只会表现为"解码失败"。所以 `/anime/media/**`
 * 从 `sra-admin.excludes` 放行后，这里自己校验登录、自己写 401/404/416/500。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Controller
@Mapping("/anime/media")
public class AniMediaController {

    private static final Logger log = LoggerFactory.getLogger(AniMediaController.class);

    @Inject
    private MediaScanner mediaScanner;

    @Inject
    private MediaStreamer mediaStreamer;

    @Db
    private LightDao lightDao;

    /**
     * 触发全量扫描（管理员）
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("/scan")
    public ApiResult<MediaScanStatus> scan() {
        mediaScanner.scanAllAsync(MediaScanner.SOURCE_SCAN);
        return ApiResult.ok(mediaScanner.getStatus());
    }

    /**
     * 触发单部扫描（管理员）
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("/scan/{opusId}")
    public ApiResult<MediaScanStatus> scanOpus(@Path("opusId") BigInteger opusId) {
        mediaScanner.scanOpusAsync(opusId, MediaScanner.SOURCE_SCAN);
        return ApiResult.ok(mediaScanner.getStatus());
    }

    /**
     * 扫描状态（登录即可）
     */
    @Get
    @Mapping("/scan/status")
    public ApiResult<MediaScanStatus> scanStatus() {
        return ApiResult.ok(mediaScanner.getStatus());
    }

    /**
     * 媒体流：Range / 206 / HEAD / 416；鉴权走 Sa-Token cookie
     */
    @Mapping(value = "/{mediaId}/stream", method = {MethodType.GET, MethodType.HEAD})
    public void stream(@Path("mediaId") BigInteger mediaId, Context ctx) throws IOException {
        // 这个路由被拦截器放行，必须在这里自校验；失败直接写状态码，不抛异常
        try {
            StpUtil.checkLogin();
        } catch (Exception e) {
            mediaStreamer.writeError(ctx, 401, "未登录");
            return;
        }
        AniMedia media;
        try {
            media = lightDao.loadById(AniMedia.class, mediaId);
        } catch (Exception e) {
            log.error("读取媒体失败 mediaId={}", mediaId, e);
            mediaStreamer.writeError(ctx, 500, "读取媒体失败");
            return;
        }
        if (media == null || (media.getIsDeleted() != null && media.getIsDeleted() == 1)) {
            mediaStreamer.writeError(ctx, 404, "资源不存在");
            return;
        }
        mediaStreamer.stream(media, ctx);
    }
}
