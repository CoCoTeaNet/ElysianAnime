package net.cocotea.elysiananime.api.anime.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.elysiananime.api.anime.model.vo.AniUpdateVO;
import net.cocotea.elysiananime.common.model.ApiPage;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作品更新动态（全站共享，登录即可见）
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Component("aniOpusUpdateService")
public class AniOpusUpdateService {

    /**
     * latest 接口的条数上限
     */
    private static final int LATEST_LIMIT_MAX = 50;

    @Db
    private LightDao lightDao;

    /**
     * 最近更新条目，按 create_time desc
     */
    public List<AniUpdateVO> latest(int limit) {
        int size = Math.min(Math.max(limit, 1), LATEST_LIMIT_MAX);
        Page<AniUpdateVO> fastPage = new Page<>();
        fastPage.setPageNo(1L);
        fastPage.setPageSize(size);
        Map<String, Object> emptyParams = new HashMap<>(2);
        Page<AniUpdateVO> page = lightDao.findPage(fastPage, "ani_update_findList", emptyParams, AniUpdateVO.class);
        return page.getRows();
    }

    /**
     * 更新动态分页，可按作品与时间范围过滤
     *
     * @param pageNo   页码，从 1 开始
     * @param pageSize 每页条数
     * @param opusId   作品ID，可空
     * @param start    起始时间，可空
     * @param end      截止时间，可空
     */
    public ApiPage<AniUpdateVO> page(int pageNo, int pageSize, String opusId, String start, String end) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("opusId", StrUtil.trimToNull(opusId));
        params.put("start", StrUtil.isBlank(start) ? null : DateUtil.parse(start));
        params.put("end", StrUtil.isBlank(end) ? null : DateUtil.parse(end));

        Page<AniUpdateVO> fastPage = new Page<>();
        fastPage.setPageNo((long) Math.max(pageNo, 1));
        fastPage.setPageSize(Math.min(Math.max(pageSize, 1), 100));
        Page<AniUpdateVO> page = lightDao.findPage(fastPage, "ani_update_findList", params, AniUpdateVO.class);
        return ApiPage.rest(page);
    }
}
