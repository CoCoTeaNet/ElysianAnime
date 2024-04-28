package net.cocotea.janime.api.anime.service;

import net.cocotea.janime.api.anime.model.dto.AniUserOpusAddDTO;
import net.cocotea.janime.api.anime.model.dto.AniUserOpusPageDTO;
import net.cocotea.janime.api.anime.model.dto.AniUserOpusUpdateDTO;
import net.cocotea.janime.api.anime.model.po.AniUserOpus;
import net.cocotea.janime.api.anime.model.vo.AniUserOpusSharesVO;
import net.cocotea.janime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.service.BaseService;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 用户作品关联表
 *
 * @author CoCoTea 572315466@qq.com
 * @since 1.2.1 2023-03-13
 */
public interface AniUserOpusService extends BaseService<ApiPage<AniUserOpusVO>, AniUserOpusPageDTO, AniUserOpusAddDTO, AniUserOpusUpdateDTO> {
    /**
     * 用户追番
     */
    boolean follow(BigInteger opusId) throws BusinessException;

    /**
     * 查找 用户的追番列表
     */
    ApiPage<AniUserOpusVO> listByUser(AniUserOpusPageDTO param);

    /**
     * 更新播放进度
     *
     * @param param {@link AniUserOpusUpdateDTO}
     * @return true or false
     */
    boolean updateProgress(AniUserOpusUpdateDTO param);

    /**
     * 查找追番的用户邮箱
     *
     * @param opusId 作品ID
     * @return 邮箱集合
     */
    List<String> findFollowsEmail(BigInteger opusId);

    /**
     * 通关作品id和用户ID获取用户作品关联对象
     *
     * @param opusId 作品ID
     * @param userId 用户ID
     * @return {@link AniUserOpus}
     */
    AniUserOpus getByOpusIdAndUserId(BigInteger opusId, BigInteger userId);

    /**
     * 分享自己追番的作品
     *
     * @return 成功true
     */
    boolean share(BigInteger opusId) throws BusinessException;

    /**
     * 获取用户分享的番剧
     *
     * @param limits 限制条数
     * @return {@link AniUserOpusSharesVO}
     */
    List<AniUserOpusSharesVO> listFromShares(int limits);

}