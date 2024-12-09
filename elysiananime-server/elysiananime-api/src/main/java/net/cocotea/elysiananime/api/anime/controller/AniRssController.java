package net.cocotea.elysiananime.api.anime.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.json.JSONObject;
import net.cocotea.elysiananime.api.anime.model.dto.AniRssDTO;
import net.cocotea.elysiananime.api.anime.rss.model.MkXmlDetail;
import net.cocotea.elysiananime.api.anime.rss.model.RenameInfo;
import net.cocotea.elysiananime.common.annotation.LogPersistence;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.api.anime.rss.MiKanRss;
import org.noear.solon.annotation.*;

import java.math.BigInteger;
import java.util.List;

@Mapping("/anime/rss")
@Controller
public class AniRssController {

    @Inject
    private MiKanRss miKanRss;

    /**
     * 提交番剧订阅
     *
     * @param rssDTO {@link AniRssDTO}
     * @return 成功true
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @Post
    @Mapping("/subscribe")
    public ApiResult<Boolean> subscribe(@Body AniRssDTO rssDTO) throws BusinessException {
        boolean r = miKanRss.subscribe(rssDTO);
        return ApiResult.ok(r);
    }

    /**
     * 关闭番剧订阅
     *
     * @param opusId 作品id
     * @return 成功true
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @Post @Mapping("/{opusId}/closeSubscribe")
    public ApiResult<Boolean> closeSubscribe(@Path("opusId") BigInteger opusId) {
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

    /**
     * 获取RSS执行状态
     */
    @Get @Mapping("/getRssWorkStatus")
    public ApiResult<JSONObject> getRssWorkStatus() {
        JSONObject msg = miKanRss.getRssWorkStatus();
        return ApiResult.ok(msg);
    }

    /**
     * 根据配置的规则获取重命名结果
     *
     * @param rssDTO {@link AniRssDTO}
     * @return {@link List<RenameInfo>}
     */
    @Post
    @Mapping("/getRenames")
    public ApiResult<List<RenameInfo>> getRenames(@Body AniRssDTO rssDTO) throws BusinessException {
        List<RenameInfo> list = miKanRss.getRenames(rssDTO);
        return ApiResult.ok(list);
    }

}
