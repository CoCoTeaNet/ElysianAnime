package net.cocotea.elysiananime.api.anime.service;

import net.cocotea.elysiananime.api.anime.model.dto.AniUserFeedAddDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserFeedPageDTO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserFeedVO;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;

import java.math.BigInteger;
import java.util.List;

public interface AniUserFeedService {

    /**
     * 投喂番剧给用户
     */
    boolean feed(AniUserFeedAddDTO addDTO) throws BusinessException;

    /**
     * 获取被投喂的番剧列表
     */
    ApiPage<AniUserFeedVO> listByUser(AniUserFeedPageDTO pageDTO);

    /**
     * 获取投喂出去的番剧列表
     */
    ApiPage<AniUserFeedVO> listSent(AniUserFeedPageDTO pageDTO);

    /**
     * 取消投喂
     */
    boolean delete(BigInteger id) throws BusinessException;
}
