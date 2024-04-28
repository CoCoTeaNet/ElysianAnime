package net.cocotea.janime.api.anime.service.impl;

import cn.hutool.core.util.StrUtil;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.conditions.update.LambdaUpdateWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import com.sagframe.sagacity.sqltoy.plus.multi.MultiWrapper;
import com.sagframe.sagacity.sqltoy.plus.multi.model.LambdaColumn;
import net.cocotea.janime.api.anime.model.po.AniOpusTag;
import net.cocotea.janime.api.anime.model.po.AniTag;
import net.cocotea.janime.api.anime.service.AniTagService;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.util.LoginUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.hutool.poi.excel.sax.AttributeName.s;

@Service
public class AniTagServiceImpl implements AniTagService {

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Override
    public List<BigInteger> findTagIds(List<String> tagNames) {
        MultiWrapper lambdaWrapper = Wrappers.lambdaMultiWrapper(AniOpusTag.class)
                .select(LambdaColumn.of(AniOpusTag::getOpusId))
                .from(AniOpusTag.class)
                .leftJoin(AniTag.class).on().eq(AniOpusTag::getTagId, AniTag::getId)
                .where()
                .eq(AniTag::getIsDeleted, IsEnum.N.getCode())
                .in(AniTag::getTagName, tagNames)
                .groupBy(AniOpusTag::getOpusId);
        List<AniOpusTag> list = sqlToyHelperDao.findList(lambdaWrapper);
        return list.stream().map(AniOpusTag::getOpusId).collect(Collectors.toList());
    }

    @Override
    public List<AniTag> findByOpusId(BigInteger id) {
        LambdaQueryWrapper<AniOpusTag> aniOpusTagLambdaQueryWrapper = Wrappers.lambdaWrapper(AniOpusTag.class)
                .eq(AniOpusTag::getOpusId, id);
        List<AniOpusTag> list = sqlToyHelperDao.findList(aniOpusTagLambdaQueryWrapper);
        List<BigInteger> ids = list.stream().map(AniOpusTag::getTagId).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyList();
        } else {
            LambdaQueryWrapper<AniTag> lambdaWrapper = Wrappers.lambdaWrapper(AniTag.class)
                    .in(AniTag::getId, ids);
            return sqlToyHelperDao.findList(lambdaWrapper);
        }
    }

    @Override
    public void saveTagOfOpus(List<AniTag> aniTags, BigInteger opusId) throws BusinessException {
        if (opusId == null) {
            throw new BusinessException("作品ID为空");
        }
        for (AniTag aniTag : aniTags) {
            if (StrUtil.isBlank(aniTag.getTagName())) {
                throw new BusinessException("标签名称为空");
            }
            // 新增标签
            LambdaQueryWrapper<AniTag> wrapper = Wrappers.lambdaWrapper(AniTag.class).eq(AniTag::getTagName, s);
            AniTag aniTagExist = sqlToyHelperDao.findOne(wrapper);
            if (aniTagExist == null) {
                sqlToyHelperDao.save(aniTag);
            } else {
                aniTag = aniTagExist;
            }
            // 关联标签
            LambdaQueryWrapper<AniOpusTag> aniOpusTagLambdaQueryWrapper = Wrappers.lambdaWrapper(AniOpusTag.class)
                    .eq(AniOpusTag::getOpusId, opusId)
                    .eq(AniOpusTag::getTagId, aniTag.getId());
            AniOpusTag aniOpusTag = sqlToyHelperDao.findOne(aniOpusTagLambdaQueryWrapper);
            if (aniOpusTag == null) {
                AniOpusTag opusTag = new AniOpusTag().setOpusId(opusId).setTagId(aniTag.getId());
                sqlToyHelperDao.save(opusTag);
            }
        }
    }

}
