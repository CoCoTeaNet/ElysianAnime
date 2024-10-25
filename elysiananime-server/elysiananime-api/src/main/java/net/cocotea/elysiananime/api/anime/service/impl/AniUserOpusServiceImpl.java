package net.cocotea.elysiananime.api.anime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusAddDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusPageDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusUpdateDTO;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.model.po.AniTag;
import net.cocotea.elysiananime.api.anime.model.po.AniUserOpus;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusSharesVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.elysiananime.api.anime.service.AniOpusService;
import net.cocotea.elysiananime.api.anime.service.AniTagService;
import net.cocotea.elysiananime.api.anime.service.AniUserOpusService;
import net.cocotea.elysiananime.api.system.model.po.SysUser;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.ReadStatusEnum;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.properties.DefaultProp;
import net.cocotea.elysiananime.util.LoginUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityUpdate;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

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
@Component
public class AniUserOpusServiceImpl implements AniUserOpusService {

    @Inject
    private DefaultProp defaultProp;

    @Db
    private LightDao lightDao;

    @Inject
    private AniTagService aniTagService;

    @Inject
    private AniOpusService aniOpusService;

    @Override
    public boolean add(AniUserOpusAddDTO param) {
        AniUserOpus aniUserOpus = Convert.convert(AniUserOpus.class, param);
        Object save = lightDao.save(aniUserOpus);
        return save != null;
    }

    @Tran
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
        long count = lightDao.update(aniUserOpus);
        return count > 0;
    }

    @Override
    public ApiPage<AniUserOpusVO> listByPage(AniUserOpusPageDTO pageDTO) {
        return null;
    }

    @Override
    public boolean delete(BigInteger id) {
        return lightDao.delete(new AniUserOpus().setId(id)) > 0;
    }

    @Tran
    @Override
    public boolean follow(BigInteger opusId) throws BusinessException {
        BigInteger loginId = LoginUtils.loginId();

        AniOpus aniOpus = aniOpusService.loadById(opusId);
        if (aniOpus == null) {
            throw new BusinessException("作品不存在");
        }

        Map<String, Object> mapDTO = MapUtil.newHashMap(2);
        mapDTO.put("opusId", opusId);
        mapDTO.put("userId", loginId);
        AniUserOpus userOpusExist = lightDao.findOne("ani_user_opus_findList", mapDTO, AniUserOpus.class);

        if (userOpusExist == null) {
            // 关注番剧
            AniUserOpus userOpus = new AniUserOpus()
                    .setUserId(loginId)
                    .setOpusId(opusId)
                    .setReadStatus(ReadStatusEnum.NOT_READ.getCode())
                    .setIsShare(IsEnum.N.getCode())
                    .setReadingNum(1)
                    .setReadingTime(BigInteger.valueOf(0L));
            lightDao.save(userOpus);
            return true;
        } else {
            // 取消关注
            return delete(userOpusExist.getId());
        }
    }

    @Override
    public ApiPage<AniUserOpusVO> listByUser(AniUserOpusPageDTO pageDTO) {
        BigInteger loginId = LoginUtils.loginIdEx();
        Map<String, Object> mapDTO = BeanUtil.beanToMap(pageDTO);
        mapDTO.put("loginId", loginId);
        Page<AniUserOpusVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ani_user_opus_listByUser", mapDTO, AniUserOpusVO.class);
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
                AniUserOpus userOpus = lightDao.load(new AniUserOpus().setId(id));
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
        Map<String, Object> dtoMap = new HashMap<>(1);
        dtoMap.put("opusId", opusId);
        List<SysUser> list = lightDao.find("ani_user_opus_findFollowsEmail", dtoMap, SysUser.class);
        return list.stream().map(SysUser::getEmail).collect(Collectors.toList());
    }

    @Override
    public AniUserOpus getByOpusIdAndUserId(BigInteger opusId, BigInteger userId) {
        Map<String, Object> mapDTO = MapUtil.newHashMap(2);
        mapDTO.put("opusId", opusId);
        mapDTO.put("userId", userId);
        return lightDao.findOne("ani_user_opus_findList", mapDTO, AniUserOpus.class);
    }

    @Override
    public boolean share(BigInteger opusId) throws BusinessException {
        AniOpus aniOpus = lightDao.load(new AniOpus().setId(opusId));
        if (aniOpus == null) {
            throw new BusinessException(String.format("ID[%s]作品不存在", opusId));
        }
        BigInteger loginId = LoginUtils.loginId();

        Map<String, Object> mapDTO = MapUtil.newHashMap(2);
        mapDTO.put("opusId", opusId);
        mapDTO.put("userId", loginId);
        AniUserOpus userOpus = lightDao.findOne("ani_user_opus_findList", mapDTO, AniUserOpus.class);

        if (userOpus == null) {
            return false;
        }

        EntityUpdate entityUpdate = EntityUpdate.create();
        if (userOpus.getIsShare() == IsEnum.Y.getCode().intValue()) {
            // 不推荐
            entityUpdate.set("is_share", IsEnum.N.getCode());
        } else {
            // 推荐
            entityUpdate.set("is_share", IsEnum.Y.getCode());
        }
        entityUpdate.where("opus_id = :opusId and user_id = :userId")
                .values(new AniUserOpus().setOpusId(opusId).setUserId(loginId));
        long update = lightDao.updateByQuery(AniUserOpus.class, entityUpdate);
        return update > 0;
    }

    @Override
    public List<AniUserOpusSharesVO> listFromShares(int limits) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("limits", limits);
        params.put("isShare", IsEnum.Y.getCode());
        List<AniUserOpusSharesVO> shares = lightDao.find("ani_user_opus_findByShares", params, AniUserOpusSharesVO.class);
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