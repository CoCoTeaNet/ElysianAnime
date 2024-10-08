package net.cocotea.elysiananime.api.anime.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.json.JSONObject;
import net.cocotea.elysiananime.api.anime.model.dto.AniRssDTO;
import net.cocotea.elysiananime.api.anime.rss.model.MkXmlDetail;
import net.cocotea.elysiananime.common.annotation.LogPersistence;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.api.anime.rss.MiKanRss;
import org.noear.solon.annotation.*;

import java.math.BigInteger;

@Mapping("/anime/rss")
@Controller
public class AniRssController {

    @Inject
    private MiKanRss miKanRss;

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @Post
    @Mapping("/subscribe")
    public ApiResult<?> subscribe(@Body AniRssDTO rssDTO) throws BusinessException {
        boolean r = miKanRss.subscribe(rssDTO);
        return ApiResult.ok(r);
    }

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @Post @Mapping("/{opusId}/closeSubscribe")
    public ApiResult<?> closeSubscribe(@Path("opusId") BigInteger opusId) {
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
    @Get @Mapping("/getMkXmlDetail")
    public ApiResult<MkXmlDetail> getMkXmlDetail(@Param String rssUrl) {
        MkXmlDetail detail = miKanRss.doParseMkXmlDetail(rssUrl);
        return ApiResult.ok(detail);
    }

    @Get @Mapping("/getRssWorkStatus")
    public ApiResult<JSONObject> getRssWorkStatus() {
        JSONObject msg = miKanRss.getRssWorkStatus();
        return ApiResult.ok(msg);
    }

}
