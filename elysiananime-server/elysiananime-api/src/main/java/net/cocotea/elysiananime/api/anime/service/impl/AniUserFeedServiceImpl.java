package net.cocotea.elysiananime.api.anime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserFeedAddDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserFeedPageDTO;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.model.po.AniUserFeed;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserFeedVO;
import net.cocotea.elysiananime.api.anime.service.AniOpusService;
import net.cocotea.elysiananime.api.anime.service.AniUserFeedService;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.util.LoginUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class AniUserFeedServiceImpl implements AniUserFeedService {

    @Db
    private LightDao lightDao;

    @Inject
    private AniOpusService aniOpusService;

    @Override
    public boolean feed(AniUserFeedAddDTO addDTO) throws BusinessException {
        BigInteger loginId = LoginUtils.loginId();
        if (loginId == null) {
            throw new BusinessException("用户未登录");
        }

        AniOpus aniOpus = aniOpusService.loadById(addDTO.getOpusId());
        if (aniOpus == null) {
            throw new BusinessException("作品不存在");
        }

        if (loginId.equals(addDTO.getToUserId())) {
            throw new BusinessException("不能投喂给自己");
        }

        AniUserFeed feed = new AniUserFeed()
                .setFromUserId(loginId)
                .setToUserId(addDTO.getToUserId())
                .setOpusId(addDTO.getOpusId())
                .setCreateTime(LocalDateTime.now())
                .setIsDeleted(0);
        Object save = lightDao.save(feed);
        return save != null;
    }

    @Override
    public ApiPage<AniUserFeedVO> listByUser(AniUserFeedPageDTO pageDTO) {
        BigInteger loginId = LoginUtils.loginIdEx();
        Map<String, Object> mapDTO = BeanUtil.beanToMap(pageDTO);
        mapDTO.put("toUserId", loginId);
        Page<AniUserFeedVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ani_user_feed_listByUser", mapDTO, AniUserFeedVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public ApiPage<AniUserFeedVO> listSent(AniUserFeedPageDTO pageDTO) {
        BigInteger loginId = LoginUtils.loginIdEx();
        Map<String, Object> mapDTO = BeanUtil.beanToMap(pageDTO);
        mapDTO.put("fromUserId", loginId);
        Page<AniUserFeedVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ani_user_feed_listSent", mapDTO, AniUserFeedVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) throws BusinessException {
        return lightDao.delete(new AniUserFeed().setId(id)) > 0;
    }
}
