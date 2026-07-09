package net.cocotea.elysiananime.api.anime.controller;

import net.cocotea.elysiananime.api.anime.model.dto.AniUserFeedAddDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserFeedPageDTO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserFeedVO;
import net.cocotea.elysiananime.api.anime.service.AniUserFeedService;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import org.noear.solon.annotation.*;

import java.math.BigInteger;

@Mapping("/anime/feed")
@Controller
public class AniUserFeedController {

    @Inject
    private AniUserFeedService aniUserFeedService;

    @Post @Mapping("/add")
    public ApiResult<?> feed(@Body AniUserFeedAddDTO addDTO) throws BusinessException {
        boolean b = aniUserFeedService.feed(addDTO);
        return ApiResult.flag(b);
    }

    @Post @Mapping("/listByUser")
    public ApiResult<?> listByUser(@Body AniUserFeedPageDTO param) throws BusinessException {
        ApiPage<AniUserFeedVO> r = aniUserFeedService.listByUser(param);
        return ApiResult.ok(r);
    }

    @Post @Mapping("/listSent")
    public ApiResult<?> listSent(@Body AniUserFeedPageDTO param) throws BusinessException {
        ApiPage<AniUserFeedVO> r = aniUserFeedService.listSent(param);
        return ApiResult.ok(r);
    }

    @Post @Mapping("/delete/{id}")
    public ApiResult<?> delete(@Path("id") BigInteger id) throws BusinessException {
        boolean b = aniUserFeedService.delete(id);
        return ApiResult.flag(b);
    }
}
