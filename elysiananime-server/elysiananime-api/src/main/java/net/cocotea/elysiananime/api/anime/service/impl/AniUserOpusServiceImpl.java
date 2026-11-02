package net.cocotea.elysiananime.api.anime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusAddDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusPageDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusUpdateDTO;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.model.po.AniTag;
import net.cocotea.elysiananime.api.anime.model.po.AniUserOpus;
import net.cocotea.elysiananime.api.anime.model.vo.AniOpusTagVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusSharesVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.elysiananime.api.anime.playback.AniPlaybackService;
import net.cocotea.elysiananime.api.anime.service.AniOpusService;
import net.cocotea.elysiananime.api.anime.service.AniTagService;
import net.cocotea.elysiananime.api.anime.service.AniUserOpusService;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.ReadStatusEnum;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.util.LoginUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityUpdate;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
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

    @Db
    private LightDao lightDao;

    @Inject
    private AniTagService aniTagService;

    @Inject
    private AniOpusService aniOpusService;

    @Inject
    private AniPlaybackService aniPlaybackService;

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

    /**
     * 更新追番记录。
     * <p>
     * 带 {@code readStatus} 时走手动改状态规则（见 doc/plan/playback-media-rework.md §5.2）：
     * 清零所有集的累计观看时长，改为「已看完」时同步 last_episode_key / last_media_id。
     * <p>
     * 优先用 id 定位，传 opusId 时按 (user_id, opus_id) 定位自己的记录。
     */
    @Override
    public boolean update(AniUserOpusUpdateDTO updateDTO) throws BusinessException {
        AniUserOpus userOpus = resolveOwnedUserOpus(updateDTO);
        if (updateDTO.getReadStatus() != null) {
            boolean applied = aniPlaybackService.applyManualStatus(userOpus, updateDTO.getReadStatus());
            updateOtherFields(userOpus, updateDTO);
            return applied;
        }
        return updateOtherFields(userOpus, updateDTO);
    }

    private AniUserOpus resolveOwnedUserOpus(AniUserOpusUpdateDTO updateDTO) throws BusinessException {
        BigInteger loginId = LoginUtils.loginIdEx();
        if (loginId == null) {
            throw new BusinessException("未登录");
        }
        if (updateDTO.getId() != null) {
            return loadOwnedUserOpus(updateDTO.getId(), loginId);
        }
        if (updateDTO.getOpusId() != null) {
            AniUserOpus userOpus = getByOpusIdAndUserId(updateDTO.getOpusId(), loginId);
            if (userOpus == null) {
                throw new BusinessException("无权操作该记录");
            }
            return userOpus;
        }
        throw new BusinessException("关联id为空");
    }

    /**
     * 只更新与观看状态无关的字段，条件带上 user_id 防止越权
     */
    private boolean updateOtherFields(AniUserOpus userOpus, AniUserOpusUpdateDTO updateDTO) {
        if (updateDTO.getIsShare() == null) {
            return true;
        }
        lightDao.executeSql("""
                        update ani_user_opus
                        set is_share = coalesce(?, is_share)
                        where id = ? and user_id = ?
                        """,
                updateDTO.getIsShare(), userOpus.getId(), userOpus.getUserId());
        return true;
    }

    /**
     * 取出指定用户的追番记录，越权直接拒绝
     */
    AniUserOpus loadOwnedUserOpus(BigInteger userOpusId, BigInteger loginId) throws BusinessException {
        if (userOpusId == null) {
            throw new BusinessException("关联id为空");
        }
        AniUserOpus userOpus = lightDao.loadById(AniUserOpus.class, userOpusId);
        if (userOpus == null || !loginId.equals(userOpus.getUserId())) {
            throw new BusinessException("无权操作该记录");
        }
        return userOpus;
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
                    .setCreateTime(LocalDateTime.now());
            lightDao.save(userOpus);
            return true;
        } else {
            // 已追番：幂等确认，不再当开关用，取消走 unfollow
            return true;
        }
    }

    /**
     * 取消追番
     */
    @Override
    public boolean unfollow(BigInteger opusId) throws BusinessException {
        if (opusId == null) {
            throw new BusinessException("作品id为空");
        }
        BigInteger loginId = LoginUtils.loginIdEx();
        if (loginId == null) {
            throw new BusinessException("未登录");
        }
        AniUserOpus userOpus = getByOpusIdAndUserId(opusId, loginId);
        if (userOpus == null) {
            return false;
        }
        return delete(userOpus.getId());
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

        // 获取作品的标签
        Map<BigInteger, List<AniOpusTagVO>> tagMap = aniTagService.findByOpusIds(opusIdMap.keySet());
        for (AniUserOpusSharesVO share : shares) {
            List<AniOpusTagVO> tagVOList = tagMap.get(share.getOpusId());
            if (ObjUtil.isEmpty(tagVOList)) {
                share.setAniTagList(Collections.emptyList());
                continue;
            }
            share.setAniTagList(tagVOList);
        }

        return shares;
    }

}