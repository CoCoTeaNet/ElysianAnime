package net.cocotea.janime.api.anime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSONObject;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.conditions.update.LambdaUpdateWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.anime.model.dto.AniUserOpusAddDTO;
import net.cocotea.janime.api.anime.model.dto.AniUserOpusPageDTO;
import net.cocotea.janime.api.anime.model.dto.AniUserOpusUpdateDTO;
import net.cocotea.janime.api.anime.model.po.AniOpus;
import net.cocotea.janime.api.anime.model.po.AniTag;
import net.cocotea.janime.api.anime.model.po.AniUserOpus;
import net.cocotea.janime.api.anime.model.vo.AniUserOpusSharesVO;
import net.cocotea.janime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.janime.api.anime.service.AniTagService;
import net.cocotea.janime.api.anime.service.AniUserOpusService;
import net.cocotea.janime.api.system.model.po.SysUser;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.enums.ReadStatusEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.properties.DefaultProp;
import net.cocotea.janime.util.LoginUtils;
import org.sagacity.sqltoy.model.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * 用户作品关联表
 *
 * @author CoCoTea 572315466@qq.com
 * @version 2.0.0
 */
@Service
public class AniUserOpusServiceImpl implements AniUserOpusService {

    @Resource
    private DefaultProp defaultProp;

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Resource
    private AniTagService aniTagService;

    @Override
    public boolean add(AniUserOpusAddDTO param) {
        AniUserOpus aniUserOpus = Convert.convert(AniUserOpus.class, param);
        Object save = sqlToyHelperDao.save(aniUserOpus);
        return save != null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        for (BigInteger s : idList) {
            delete(s);
        }
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AniUserOpusUpdateDTO updateDTO) {
        AniUserOpus aniUserOpus = BeanUtil.copyProperties(updateDTO, AniUserOpus.class);
        long count = sqlToyHelperDao.update(aniUserOpus);
        return count > 0;
    }

    @Override
    public ApiPage<AniUserOpusVO> listByPage(AniUserOpusPageDTO pageDTO) {
        LambdaQueryWrapper<AniUserOpus> queryWrapper = Wrappers.lambdaWrapper(AniUserOpus.class);
        Page<AniUserOpus> page = sqlToyHelperDao.findPage(queryWrapper, pageDTO);
        List<AniUserOpusVO> list = Convert.toList(AniUserOpusVO.class, page);
        return ApiPage.rest(page, list);
    }

    @Override
    public boolean delete(BigInteger id) {
        return sqlToyHelperDao.delete(new AniUserOpus().setOpusId(id)) > 0;
    }

    @Transactional(rollbackFor = BusinessException.class)
    @Override
    public boolean follow(BigInteger opusId) throws BusinessException {
        BigInteger loginId = LoginUtils.loginId();
        LambdaQueryWrapper<AniOpus> aniOpusLambdaQueryWrapper = new LambdaQueryWrapper<>(AniOpus.class).eq(AniOpus::getId, opusId);
        long count = sqlToyHelperDao.count(aniOpusLambdaQueryWrapper);
        if (count == 0) {
            throw new BusinessException("作品不存在");
        }
        LambdaQueryWrapper<AniUserOpus> aniUserOpusLambdaQueryWrapper = new LambdaQueryWrapper<>(AniUserOpus.class).eq(AniUserOpus::getUserId, loginId).eq(AniUserOpus::getOpusId, opusId);
        count = sqlToyHelperDao.count(aniUserOpusLambdaQueryWrapper);
        if (count == 0) {
            // 关注番剧
            AniUserOpus userOpus = new AniUserOpus()
                    .setUserId(loginId)
                    .setOpusId(opusId)
                    .setReadStatus(ReadStatusEnum.NOT_READ.getCode())
                    .setIsShare(IsEnum.N.getCode())
                    .setReadingNum(1)
                    .setReadingTime(BigInteger.valueOf(0L));
            sqlToyHelperDao.save(userOpus);
            return true;
        } else {
            // 取消关注
            return sqlToyHelperDao.delete(aniUserOpusLambdaQueryWrapper) > 0;
        }
    }

    @Override
    public ApiPage<AniUserOpusVO> listByUser(AniUserOpusPageDTO pageDTO) {
        BigInteger loginId = LoginUtils.loginIdEx();
        JSONObject mapDTO = Convert.convert(JSONObject.class, pageDTO);
        mapDTO.put("loginId", loginId);
        Page<AniUserOpusVO> page = sqlToyHelperDao.findPageBySql(pageDTO, "ani_user_opus_listByUser", mapDTO, AniUserOpusVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean updateProgress(AniUserOpusUpdateDTO updateDTO) {
        Long readingTime = updateDTO.getReadingTime();
        Long autoReadingTime = defaultProp.getAutoReadingTime();
        // 根据观看时长自动更新观看状态
        if (readingTime != null && autoReadingTime != null) {
            if (readingTime >= autoReadingTime) {
                BigInteger id = updateDTO.getId();
                AniUserOpus userOpus = sqlToyHelperDao.load(new AniUserOpus().setId(id));
                Integer readStatus = userOpus.getReadStatus();
                if (readStatus == ReadStatusEnum.NOT_READ.getCode().intValue()) {
                    updateDTO.setReadStatus(ReadStatusEnum.READING.getCode());
                }
            }
        }
        return update(updateDTO);
    }

    @Override
    public List<String> findFollowsEmail(BigInteger opusId) {
        JSONObject dto = Convert.convert(JSONObject.class, new AniUserOpus().setOpusId(opusId));
        List<SysUser> list = sqlToyHelperDao.findBySql("ani_user_opus_findFollowsEmail", dto, SysUser.class);
        return list.stream().map(SysUser::getEmail).collect(Collectors.toList());
    }

    @Override
    public AniUserOpus getByOpusIdAndUserId(BigInteger opusId, BigInteger userId) {
        LambdaQueryWrapper<AniUserOpus> queryWrapper = Wrappers.lambdaWrapper(AniUserOpus.class)
                .eq(AniUserOpus::getOpusId, opusId)
                .eq(AniUserOpus::getUserId, userId);
        return sqlToyHelperDao.findOne(queryWrapper);
    }

    @Override
    public boolean share(BigInteger opusId) throws BusinessException {
        AniOpus aniOpus = sqlToyHelperDao.load(new AniOpus().setId(opusId));
        if (aniOpus == null) {
            throw new BusinessException(String.format("ID[%s]作品不存在", opusId));
        }
        BigInteger loginId = LoginUtils.loginId();
        LambdaQueryWrapper<AniUserOpus> queryWrapper = Wrappers.lambdaWrapper(AniUserOpus.class)
                .eq(AniUserOpus::getOpusId, opusId)
                .eq(AniUserOpus::getUserId, loginId);
        AniUserOpus userOpus = sqlToyHelperDao.findOne(queryWrapper);
        if (userOpus == null) {
            return false;
        }
        AniUserOpus aniUserOpus = new AniUserOpus();
        if (userOpus.getIsShare() == IsEnum.Y.getCode().intValue()) {
            // 不分享
            aniUserOpus.setIsShare(IsEnum.N.getCode());
        } else {
            // 推荐
            aniUserOpus.setIsShare(IsEnum.Y.getCode());
        }
        LambdaUpdateWrapper<AniUserOpus> updateWrapper = Wrappers.lambdaUpdateWrapper(AniUserOpus.class)
                .eq(AniUserOpus::getOpusId, opusId)
                .eq(AniUserOpus::getUserId, loginId);
        long update = sqlToyHelperDao.update(aniUserOpus, updateWrapper);
        return update > 0;
    }

    @Override
    public List<AniUserOpusSharesVO> listFromShares(int limits) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("limits", limits);
        params.put("isShare", IsEnum.Y.getCode());
        List<AniUserOpusSharesVO> shares = sqlToyHelperDao.findBySql("ani_user_opus_findByShares", params, AniUserOpusSharesVO.class);
        Map<BigInteger, List<AniUserOpusSharesVO>> opusIdMap = shares.stream().collect(groupingBy(AniUserOpusSharesVO::getOpusId));
        shares = new ArrayList<>(opusIdMap.size());
        for (List<AniUserOpusSharesVO> sharesItem : opusIdMap.values()) {
            if (sharesItem.isEmpty()) {
                continue;
            }
            List<String> userList = sharesItem.stream().map(AniUserOpusSharesVO::getShareUser).collect(Collectors.toList());
            AniUserOpusSharesVO sharesVO = sharesItem.get(0);
            sharesVO.setShareUserList(userList);
            shares.add(sharesVO);
        }
        for (AniUserOpusSharesVO share : shares) {
            // 查找标签
            List<AniTag> tagList = aniTagService.findByOpusId(share.getOpusId());
            share.setAniTagList(tagList);
        }
        return shares;
    }

}