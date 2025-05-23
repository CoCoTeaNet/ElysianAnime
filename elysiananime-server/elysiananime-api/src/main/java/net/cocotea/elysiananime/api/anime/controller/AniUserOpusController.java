package net.cocotea.elysiananime.api.anime.controller;

import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusPageDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusUpdateDTO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusSharesVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.elysiananime.api.anime.service.AniUserOpusService;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import org.noear.solon.annotation.*;

import java.math.BigInteger;
import java.util.List;

@Mapping("/anime/userOpus")
@Controller
public class AniUserOpusController {

    @Inject
    private AniUserOpusService aniUserOpusService;

    @Post
    @Mapping("/{opusId}/follow")
    public ApiResult<?> follow(@Path("opusId") BigInteger opusId) throws BusinessException {
        boolean b = aniUserOpusService.follow(opusId);
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
     * 更新观看进度
     *
     * @param updateDTO {@link AniUserOpusUpdateDTO}
     * @return true更新成功
     */
    @Post @Mapping("/updateProgress")
    public ApiResult<Boolean> updateProgress(@Body AniUserOpusUpdateDTO updateDTO) {
        boolean r = aniUserOpusService.updateProgress(updateDTO);
        return ApiResult.ok(r);
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
