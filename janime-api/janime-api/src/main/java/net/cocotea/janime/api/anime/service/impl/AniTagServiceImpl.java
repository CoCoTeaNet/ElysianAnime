package net.cocotea.janime.api.anime.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.janime.api.anime.model.po.AniOpusTag;
import net.cocotea.janime.api.anime.model.po.AniTag;
import net.cocotea.janime.api.anime.service.AniTagService;
import net.cocotea.janime.common.model.BusinessException;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AniTagServiceImpl implements AniTagService {

    @Db
    private LightDao lightDao;

    @Override
    public List<AniTag> findByOpusId(BigInteger id) {
        Map<String, Object> aniOpusTagMapDTO = MapUtil.newHashMap(1);
        aniOpusTagMapDTO.put("opusId", id);
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
