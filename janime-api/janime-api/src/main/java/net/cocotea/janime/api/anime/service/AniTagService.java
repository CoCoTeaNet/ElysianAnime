package net.cocotea.janime.api.anime.service;

import net.cocotea.janime.api.anime.model.po.AniTag;
import net.cocotea.janime.common.model.BusinessException;

import java.math.BigInteger;
import java.util.List;

/**
 * 标签服务接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
public interface AniTagService {

    /**
     * 通关作品查找关联的标签
     *
     * @param id 作品ID
     * @return 标签
     */
    List<AniTag> findByOpusId(BigInteger id);

    /**
     * 保存作品标签
     *
     * @param aniTags 标签
     * @param opusId  作品ID
     */
    void saveTagOfOpus(List<AniTag> aniTags, BigInteger opusId) throws BusinessException;
}
