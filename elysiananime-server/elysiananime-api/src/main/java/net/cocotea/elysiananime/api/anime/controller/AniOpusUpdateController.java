package net.cocotea.elysiananime.api.anime.controller;

import net.cocotea.elysiananime.api.anime.model.vo.AniUpdateVO;
import net.cocotea.elysiananime.api.anime.service.AniOpusUpdateService;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.ApiResult;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Param;

import java.util.List;

/**
 * 作品更新动态（登录即可见，走全局 Sa-Token 拦截器）
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Mapping("/anime/update")
@Controller
public class AniOpusUpdateController {

    /**
     * latest 未传 limit 时的默认条数
     */
    private static final int LATEST_DEFAULT = 12;

    @Inject
    private AniOpusUpdateService aniOpusUpdateService;

    /**
     * 最近更新条目（首页横滑区）
     */
    @Get
    @Mapping("/latest")
    public ApiResult<List<AniUpdateVO>> latest(@Param(value = "limit", required = false) Integer limit) {
        int size = limit == null || limit <= 0 ? LATEST_DEFAULT : limit;
        return ApiResult.ok(aniOpusUpdateService.latest(size));
    }

    /**
     * 更新动态分页（历史翻阅）
     */
    @Get
    @Mapping("/page")
    public ApiResult<ApiPage<AniUpdateVO>> page(@Param(value = "pageNo", required = false) Integer pageNo,
                                                @Param(value = "pageSize", required = false) Integer pageSize,
                                                @Param(value = "opusId", required = false) String opusId,
                                                @Param(value = "start", required = false) String start,
                                                @Param(value = "end", required = false) String end) {
        int no = pageNo == null ? 1 : pageNo;
        int size = pageSize == null ? 20 : pageSize;
        return ApiResult.ok(aniOpusUpdateService.page(no, size, opusId, start, end));
    }
}
