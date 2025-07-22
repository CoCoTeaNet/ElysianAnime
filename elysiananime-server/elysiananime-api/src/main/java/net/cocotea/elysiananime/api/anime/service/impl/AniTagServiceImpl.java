package net.cocotea.elysiananime.api.anime.service.impl;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.elysiananime.api.anime.model.po.AniOpusTag;
import net.cocotea.elysiananime.api.anime.model.po.AniTag;
import net.cocotea.elysiananime.api.anime.model.vo.AniOpusTagVO;
import net.cocotea.elysiananime.api.anime.service.AniTagService;
import net.cocotea.elysiananime.common.model.BusinessException;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class AniTagServiceImpl implements AniTagService {

    @Db
    private LightDao lightDao;

    @Override
    public List<AniTag> findByOpusId(BigInteger opusId) {
        Map<String, Object> aniOpusTagMapDTO = MapUtil.newHashMap(1);
        aniOpusTagMapDTO.put("opusId", opusId);
        List<AniOpusTag> list = lightDao.find("ani_opus_tag_findList", aniOpusTagMapDTO, AniOpusTag.class);

        List<BigInteger> ids = list.stream().map(AniOpusTag::getTagId).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> aniTagMapDTO = MapUtil.newHashMap(1);
        aniTagMapDTO.put("inId", ids);
        return lightDao.find("ani_tag_findList", aniTagMapDTO, AniTag.class);
    }

    @Override
    public Map<BigInteger, List<AniOpusTagVO>> findByOpusIds(Set<BigInteger> opusIds) {
        Map<String, Object> params = MapUtil.builder(new HashMap<String, Object>())
                .put("opusIds", opusIds)
                .build();
        List<AniOpusTagVO> opusTagVOList = lightDao.find("ani_opus_tag_findListByOpusIds", params, AniOpusTagVO.class);
        return opusTagVOList.stream().collect(groupingBy(AniOpusTagVO::getOpusId));
    }

    @Override
    public void saveTagOfOpus(List<AniTag> aniTags, BigInteger opusId) throws BusinessException {
        if (opusId == null) {
            throw new BusinessException("作品ID为空");
        }
        for (AniTag aniTag : aniTags) {
            String tagName = aniTag.getTagName();
            if (StrUtil.isBlank(tagName)) {
                throw new BusinessException("标签名称为空");
            }
            // 新增标签
            Map<String, Object> aniTagMapDTO = MapUtil.newHashMap(1);
            aniTagMapDTO.put("tagName", tagName);
            AniTag aniTagExist = lightDao.findOne("ani_tag_findList", aniTagMapDTO, AniTag.class);
            if (aniTagExist == null) {
                lightDao.save(aniTag);
            } else {
                aniTag = aniTagExist;
            }
            // 关联标签
            Map<String, Object> aniOpusTagMapDTO = MapUtil.newHashMap(2);
            aniOpusTagMapDTO.put("opusId", opusId);
            aniOpusTagMapDTO.put("tagId", aniTag.getId());
            AniOpusTag aniOpusTag = lightDao.findOne("ani_opus_tag_findList", aniOpusTagMapDTO, AniOpusTag.class);
            if (aniOpusTag == null) {
                AniOpusTag opusTag = new AniOpusTag().setOpusId(opusId).setTagId(aniTag.getId());
                lightDao.save(opusTag);
            }
        }
    }

}
