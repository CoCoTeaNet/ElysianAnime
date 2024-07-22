package net.cocotea.janime.api.anime.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.dtflys.forest.Forest;
import net.cocotea.janime.api.anime.model.dto.AniRssDTO;
import net.cocotea.janime.api.anime.rss.model.MkXmlDetail;
import net.cocotea.janime.api.anime.rss.model.MkXmlItem;
import net.cocotea.janime.common.annotation.LogPersistence;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.api.anime.rss.MiKanRss;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/anime/rss")
public class AniRssController {

    @Resource
    private MiKanRss miKanRss;

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @PostMapping("/subscribe")
    public ApiResult<?> subscribe(@Valid @RequestBody AniRssDTO rssDTO) throws BusinessException {
        boolean r = miKanRss.subscribe(rssDTO);
        return ApiResult.ok(r);
    }

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @PostMapping("/{opusId}/closeSubscribe")
    public ApiResult<?> closeSubscribe(@PathVariable("opusId") BigInteger opusId) {
        boolean r = miKanRss.closeSubscribe(opusId);
        return ApiResult.ok(r);
    }

    /**
     * 获取资源信息详细信息
     *
     * @param rssUrl rss地址
     * @return {@link MkXmlDetail}
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @GetMapping("/getMkXmlDetail")
    public ApiResult<MkXmlDetail> getMkXmlDetail(@RequestParam String rssUrl) {
        MkXmlDetail detail = miKanRss.doParseMkXmlDetail(rssUrl);
        return ApiResult.ok(detail);
    }

}
