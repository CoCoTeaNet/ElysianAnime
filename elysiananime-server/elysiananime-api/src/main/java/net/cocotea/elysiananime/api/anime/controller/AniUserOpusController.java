package net.cocotea.elysiananime.api.anime.controller;

import net.cocotea.elysiananime.api.anime.model.dto.AniPlaybackReportDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusPageDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusUnfollowDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusUpdateDTO;
import net.cocotea.elysiananime.api.anime.model.vo.AniContinueVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniPlaybackResultVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusSharesVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.elysiananime.api.anime.playback.AniPlaybackService;
import net.cocotea.elysiananime.api.anime.service.AniUserOpusService;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.util.LoginUtils;
import org.noear.solon.annotation.*;

import java.math.BigInteger;
import java.util.List;

@Mapping("/anime/userOpus")
@Controller
public class AniUserOpusController {

    /**
     * 继续观看的条数上限
     */
    private static final int CONTINUE_LIMIT = 3;

    @Inject
    private AniUserOpusService aniUserOpusService;

    @Inject
    private AniPlaybackService aniPlaybackService;

    @Post
    @Mapping("/{opusId}/follow")
    public ApiResult<?> follow(@Path("opusId") BigInteger opusId) throws BusinessException {
        boolean b = aniUserOpusService.follow(opusId);
        return ApiResult.flag(b);
    }

    /**
     * 取消追番
     */
    @Post @Mapping("/unfollow")
    public ApiResult<?> unfollow(@Body AniUserOpusUnfollowDTO param) throws BusinessException {
        boolean b = aniUserOpusService.unfollow(param.getOpusId());
        return ApiResult.flag(b);
    }

    @Post @Mapping("/listByUser")
    public ApiResult<?> listByUser(@Body AniUserOpusPageDTO param) throws BusinessException {
        ApiPage<AniUserOpusVO> r = aniUserOpusService.listByUser(param);
        return ApiResult.ok(r);
    }

    @Post @Mapping("/update")
    public ApiResult<?> update(@Body AniUserOpusUpdateDTO updateDTO) throws BusinessException {
        boolean r = aniUserOpusService.update(updateDTO);
        return ApiResult.ok(r);
    }

    /**
     * 上报播放进度
     *
     * @param reportDTO {@link AniPlaybackReportDTO}
     * @return 状态机结果
     */
    @Post @Mapping("/progress")
    public ApiResult<AniPlaybackResultVO> progress(@Body AniPlaybackReportDTO reportDTO) throws BusinessException {
        return ApiResult.ok(aniPlaybackService.report(reportDTO));
    }

    /**
     * 继续观看
     *
     * @return 最近观看的作品，最多 3 条
     */
    @Get @Mapping("/continue")
    public ApiResult<List<AniContinueVO>> continueList() {
        List<AniContinueVO> list = aniPlaybackService.continueList(LoginUtils.loginIdEx(), CONTINUE_LIMIT);
        return ApiResult.ok(list);
    }

    /**
     * 分享自己追番的作品
     *
     * @return 成功true
     */
    @Post @Mapping("/share/{opusId}")
    public ApiResult<Boolean> share(@Path BigInteger opusId) throws BusinessException {
        boolean r = aniUserOpusService.share(opusId);
        return ApiResult.ok(r);
    }

    /**
     * 获取用户分享的番剧
     *
     * @param limits 限制条数
     * @return {@link AniUserOpusSharesVO}
     */
    @Get @Mapping("/shares/list")
    public ApiResult<List<AniUserOpusSharesVO>> listFromShares(@Param int limits) {
        if (limits <= 0 || limits > 1000) {
            limits = 100;
        }
        List<AniUserOpusSharesVO> list = aniUserOpusService.listFromShares(limits);
        return ApiResult.ok(list);
    }
}
