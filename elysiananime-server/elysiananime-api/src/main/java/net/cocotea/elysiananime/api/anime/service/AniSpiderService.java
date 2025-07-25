package net.cocotea.elysiananime.api.anime.service;

import net.cocotea.elysiananime.common.model.BusinessException;

import java.math.BigInteger;

/**
 * BGM作品信息抓取服务
 *
 * @author CoCoTea
 * @since v1
 */
public interface AniSpiderService {

    /**
     * 通过URL抓取bgm信息
     *
     * @param bgmUrl  例如：<a href="https://bgm.tv/subject/389772">...</a>
     * @param isCover 是否覆盖
     */
    @Deprecated
    boolean addAniOpusByBgmUrl(String bgmUrl, Integer isCover) throws BusinessException;

    BigInteger addOpusFromBangumi(String bgmUrl, Integer isCover) throws BusinessException;

}
