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

    BigInteger addOpusFromBangumi(String bgmUrl, Integer isCover) throws BusinessException;

    void autoFetchOpus() throws BusinessException;

}
