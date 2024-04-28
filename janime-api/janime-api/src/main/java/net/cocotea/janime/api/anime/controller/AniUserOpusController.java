package net.cocotea.janime.api.anime.controller;

import net.cocotea.janime.api.anime.model.dto.AniUserOpusPageDTO;
import net.cocotea.janime.api.anime.model.dto.AniUserOpusUpdateDTO;
import net.cocotea.janime.api.anime.model.vo.AniUserOpusSharesVO;
import net.cocotea.janime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.janime.api.anime.service.AniUserOpusService;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;

@RequestMapping("/anime/userOpus")
@RestController
public class AniUserOpusController {
    @Resource
    private AniUserOpusService aniUserOpusService;

    @PostMapping("/{opusId}/follow")
    public ApiResult<?> follow(@Valid @PathVariable("opusId") BigInteger opusId) throws BusinessException {
        boolean b = aniUserOpusService.follow(opusId);
        return ApiResult.flag(b);
    }

    @PostMapping("/listByUser")
    public ApiResult<?> listByUser(@Valid @RequestBody AniUserOpusPageDTO param) throws BusinessException {
        ApiPage<AniUserOpusVO> r = aniUserOpusService.listByUser(param);
        return ApiResult.ok(r);
    }

    @PostMapping("/update")
    public ApiResult<?> update(@Valid @RequestBody AniUserOpusUpdateDTO updateDTO) throws BusinessException {
        boolean r = aniUserOpusService.update(updateDTO);
        return ApiResult.ok(r);
    }

    @PostMapping("/updateProgress")
    public ApiResult<?> updateProgress(@Valid @RequestBody AniUserOpusUpdateDTO updateDTO) {
        boolean r = aniUserOpusService.updateProgress(updateDTO);
        return ApiResult.ok(r);
    }

    /**
     * 分享自己追番的作品
     *
     * @param shareDTO {@link AniUserOpusShareDTO}
     * @return 成功true
     */
    @PostMapping("/share/{opusId}")
    public ApiResult<Boolean> share(@PathVariable BigInteger opusId) throws BusinessException {
        boolean r = aniUserOpusService.share(opusId);
        return ApiResult.ok(r);
    }

    /**
     * 获取用户分享的番剧
     *
     * @param limits 限制条数
     * @return {@link AniUserOpusSharesVO}
     */
    @GetMapping("/shares/list")
    public ApiResult<List<AniUserOpusSharesVO>> listFromShares(@RequestParam int limits) {
        if (limits <= 0 || limits > 1000) {
            limits = 100;
        }
        List<AniUserOpusSharesVO> list = aniUserOpusService.listFromShares(limits);
        return ApiResult.ok(list);
    }
}
