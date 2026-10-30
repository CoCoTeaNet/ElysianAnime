package net.cocotea.elysiananime.api.anime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.elysiananime.api.anime.model.dto.*;
import net.cocotea.elysiananime.api.anime.model.po.AniMedia;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.model.po.AniTag;
import net.cocotea.elysiananime.api.anime.model.po.AniUserOpus;
import net.cocotea.elysiananime.api.anime.model.vo.AniOpusHomeVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniOpusVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniVideoVO;
import net.cocotea.elysiananime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.elysiananime.api.system.service.SysNotifyService;
import net.cocotea.elysiananime.api.anime.service.AniOpusService;
import net.cocotea.elysiananime.api.anime.service.AniTagService;
import net.cocotea.elysiananime.api.anime.service.AniUserOpusService;
import net.cocotea.elysiananime.common.constant.NotifyConst;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.LevelEnum;
import net.cocotea.elysiananime.common.enums.ReadStatusEnum;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.model.FileInfo;
import net.cocotea.elysiananime.properties.FileProp;
import net.cocotea.elysiananime.util.LoginUtils;
import net.cocotea.elysiananime.util.ResUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 作品
 *
 * @author CoCoTea 572315466@qq.com
 * @since 1.2.1 2023-03-07
 */
@Component
public class AniOpusServiceImpl implements AniOpusService {
    private static final Logger log = LoggerFactory.getLogger(AniOpusServiceImpl.class);

    @Db
    private LightDao lightDao;

    @Inject
    private ResUtils resUtils;

    @Inject
    private SysNotifyService sysNotifyService;

    @Inject
    private AniUserOpusService aniUserOpusService;

    @Inject
    private AniTagService aniTagService;

    @Inject
    private FileProp fileProp;

    @Override
    public boolean update(AniOpus aniOpus) {
        Long count = lightDao.update(aniOpus);
        return count > 0;
    }

    @Override
    public AniOpus loadById(BigInteger id) throws BusinessException {
        if (id == null) {
            throw new BusinessException("主键为空");
        }
        return lightDao.load(new AniOpus().setId(id));
    }

    @Override
    public AniOpus loadByNameCn(String nameCn) throws BusinessException {
        if (StrUtil.isBlank(nameCn)) {
            throw new BusinessException("名称为空");
        }
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("nameCn", nameCn);
        AniOpus aniOpus = lightDao.findOne("ani_opus_findList", map, AniOpus.class);
        return Convert.convert(AniOpus.class, aniOpus);
    }

    @Override
    public ApiPage<AniOpusHomeVO> listByUser(AniOpusHomeDTO homeDTO) {
        BigInteger loginId = LoginUtils.loginId();
        // 作品主表查询
        Map<String, Object> mapDTO = BeanUtil.beanToMap(homeDTO);
        mapDTO.put("loginId", loginId);
        // 库里没有 is_follow 列，「追番」= ani_user_opus 有对应行
        Integer isFollow = homeDTO.getIsFollow();
        mapDTO.put("followFlag", Integer.valueOf(1).equals(isFollow) ? 1 : 0);
        mapDTO.put("unfollowFlag", Integer.valueOf(0).equals(isFollow) ? 1 : 0);
        Page<AniOpusHomeVO> page = lightDao.findPage(ApiPage.create(homeDTO), "ani_opus_listByUser", mapDTO, AniOpusHomeVO.class);
        for (AniOpusHomeVO row : page.getRows()) {
            if (row.getTotalTime() == null) {
                row.setTotalTime(BigInteger.ONE);
            }
            fillUpdateState(row);
        }
        return ApiPage.rest(page);
    }

    /**
     * 按「是否追上最新一集」算 NEW 与 ✓，口径见 {@code doc/plan/playback-media-rework.md} §6
     */
    private void fillUpdateState(AniOpusHomeVO row) {
        int mediaCount = row.getMediaCount() == null ? 0 : row.getMediaCount();
        int completedCount = row.getCompletedCount() == null ? 0 : row.getCompletedCount();
        String latestKey = row.getMediaLatestKey();
        String lastKey = row.getLastEpisodeKey();
        // 看完正片后又看 SP 时 last_episode_key 会变成 SP 键，因此还要看最新正片集是否有看完的进度
        boolean caughtUp = StrUtil.isNotBlank(latestKey)
                && (latestKey.equals(lastKey) || Integer.valueOf(1).equals(row.getLatestEpisodeCompleted()));
        boolean started = StrUtil.isNotBlank(lastKey);
        row.setHasUpdate(!caughtUp && started ? 1 : 0);
        int readStatus = row.getReadStatus() == null ? 0 : row.getReadStatus();
        boolean completed = readStatus != 0 && (caughtUp || (mediaCount > 0 && completedCount >= mediaCount));
        row.setIsCompleted(completed ? 1 : 0);
        row.setDownloadNum(mediaCount);
        if (StrUtil.isBlank(row.getEpisodesNewest())) {
            row.setEpisodesNewest(StrPool.DASHED);
        }
    }

    @Override
    public boolean add(AniOpusAddDTO param) {
        AniOpus aniOpus = Convert.convert(AniOpus.class, param);
        Object save = lightDao.save(aniOpus);
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
    public boolean update(AniOpusUpdateDTO updateDTO) {
        AniOpus aniOpus = Convert.convert(AniOpus.class, updateDTO);
        Long count = lightDao.update(aniOpus);
        return count > 0;
    }

    @Override
    public ApiPage<AniOpusVO> listByPage(AniOpusPageDTO pageDTO) {
        Map<String, Object> beanDTO = BeanUtil.beanToMap(pageDTO.getAniOpus());
        Page<AniOpusVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ani_opus_JOIN_findList", beanDTO, AniOpusVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AniOpus aniOpus = new AniOpus().setId(id).setIsDeleted(IsEnum.Y.getCode());
        return lightDao.update(aniOpus) > 0;
    }

    @Override
    public AniVideoVO getOpusMedia(BigInteger opusId) {
        AniOpus aniOpus = lightDao.loadEntity(AniOpus.class, new EntityQuery().names("ID").values(opusId));
        AniVideoVO vo = Convert.convert(AniVideoVO.class, aniOpus);
        // 查找关联用户
        AniUserOpus userOpus = aniUserOpusService.getByOpusIdAndUserId(opusId, LoginUtils.loginId());
        if (userOpus != null) {
            // 已点追番
            vo.setReadStatus(userOpus.getReadStatus());
            vo.setReadingNum(userOpus.getReadingNum());
            vo.setReadingTime(userOpus.getReadingTime());
            vo.setIsFollow(IsEnum.Y.getCode());
            vo.setUserOpusId(userOpus.getId());
            vo.setIsShare(userOpus.getIsShare());
        } else {
            // 未点追番
            userOpus = new AniUserOpus();
            vo.setReadStatus(ReadStatusEnum.NOT_READ.getCode());
            vo.setReadingNum("1");
            vo.setReadingTime(BigInteger.valueOf(0));
            vo.setIsFollow(IsEnum.N.getCode());
            vo.setIsShare(IsEnum.N.getCode());
        }
        vo.setUserId(userOpus.getUserId());
        // 读媒体索引，不再扫盘
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("opusId", opusId);
        List<AniMedia> mediaRows = lightDao.find("ani_media_listByOpus", map, AniMedia.class);
        List<AniVideoVO.Media> mediaList = new ArrayList<>(mediaRows.size());
        for (AniMedia row : mediaRows) {
            mediaList.add(toMedia(row));
        }
        vo.setMediaList(mediaList);
        // 查找标签
        List<AniTag> tagList = aniTagService.findByOpusId(aniOpus.getId());
        vo.setAniTags(tagList);
        return vo;
    }

    private AniVideoVO.Media toMedia(AniMedia row) {
        return new AniVideoVO.Media()
                .setMediaId(row.getId())
                .setEpisodeKey(row.getEpisodeKey())
                .setEpisodeNo(row.getEpisodeNo())
                .setIsExtra(row.getEpisodeNo() == null ? 1 : 0)
                .setSortNo(row.getSortNo())
                .setDisplayName(row.getDisplayName())
                // 旧页面用「文件名去扩展名 + 扩展名」拼资源名，P4 前保留
                .setEpisodes(StrUtil.subBefore(row.getFileName(), '.', true))
                .setMediaType(row.getExtension())
                .setIsFolder(IsEnum.N.getCode());
    }

    @Override
    public Map<String, List<AniVideoVO.Media>> getMedias(File[] files) {
        List<AniVideoVO.Media> mediaList = new ArrayList<>(files == null ? 0 : files.length);
        // 非数字集数，一般是SP或者OVA
        List<AniVideoVO.Media> noSortMediaList = new ArrayList<>();
        if (files == null) {
            return MapBuilder.create(new HashMap<String, List<AniVideoVO.Media>>())
                    .put("mediaList", mediaList)
                    .put("noSortMediaList", noSortMediaList)
                    .build();
        }
        for (File file : files) {
            // 校验支持的媒体格式
            String[] split = file.getName().split("\\.");
            String suffix = split[split.length - 1];
            if (!fileProp.getMediaFileType().contains(suffix)) {
                continue;
            }

            // 重新拼接资源名称
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 0; i < split.length - 1; i++) {
                if (i >= split.length - 2) {
                    nameBuilder.append(split[i]);
                } else {
                    nameBuilder.append(split[i]).append(".");
                }
            }

            AniVideoVO.Media media = new AniVideoVO.Media()
                    .setEpisodes(nameBuilder.toString())
                    .setMediaType(suffix)
                    .setIsFolder(file.isDirectory() ? IsEnum.Y.getCode() : IsEnum.N.getCode());

            // 如果是集数那就排序，否则扔到最后面
            if (NumberUtil.isNumber(nameBuilder.toString())) {
                mediaList.add(media);
            } else {
                noSortMediaList.add(media);
            }
        }
        return MapBuilder.create(new HashMap<String,List<AniVideoVO.Media>>())
                .put("mediaList", mediaList)
                .put("noSortMediaList", noSortMediaList)
                .build();
    }

    @Override
    public List<AniOpus> getRssOpus(int rssStatus) {
        Map<String, Object> beanDTO = MapUtil.newHashMap(1);
        beanDTO.put("rssStatus", rssStatus);
        return lightDao.find("ani_opus_findList", beanDTO, AniOpus.class);
    }

    @Override
    public FileInfo uploadRes(BigInteger opusId, String filename) throws BusinessException {
        AniOpus opus = lightDao.load(new AniOpus().setId(opusId));
        FileInfo fileInfo = resUtils.saveRes(opus.getNameCn(), filename);
        // 发送系统通知
        SysNotifyAddDTO sysNotifyAddDTO = new SysNotifyAddDTO()
                .setTitle("【" + opus.getNameCn() + "】更新啦~~~")
                .setMemo("资源名：" + fileInfo.getFileName())
                .setJumpUrl(String.valueOf(opus.getId()))
                .setNotifyTime(DateUtil.date().toTimestamp())
                .setLevel(LevelEnum.INFO.getCode())
                .setIsGlobal(IsEnum.Y.getCode())
                .setNotifyType(NotifyConst.OPUS_UPDATE);
        sysNotifyService.addNotify(sysNotifyAddDTO);
        return fileInfo;
    }

    @Override
    public AniMedia findMedia(BigInteger opusId, String resName) {
        if (StrUtil.isBlank(resName)) {
            return null;
        }
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("opusId", opusId);
        map.put("fileName", resName);
        return lightDao.findOne("ani_media_findByFileName", map, AniMedia.class);
    }

    @Override
    public File getCover(String resName, Integer w, Integer h) throws BusinessException, IOException {
        if (StrUtil.isBlank(resName)) {
            throw new BusinessException("资源不存在");
        }

        File cover = FileUtil.file(fileProp.getAnimationCoverSavePath() + resName);
        if (!cover.exists()) {
            throw new BusinessException("文件不存在");
        }

        File outputDir = FileUtil.file(cover.getParent() + FileUtil.FILE_SEPARATOR + "scale_cache");

        if (!outputDir.exists()) {
            boolean mkdir = outputDir.mkdirs();
            if (mkdir) {
                log.info("getCover >>>>> mkdir success, outputDir: {}", outputDir.getPath());
            }
        }

        if (w == null || h == null || w <= 0 || h <= 0) {
            return cover;
        }

        String scaleFilename = StrUtil.format("{}_{}_{}", w, h, cover.getName());
        File scaleFile = FileUtil.file(outputDir.getPath() + FileUtil.FILE_SEPARATOR + scaleFilename);

        if (scaleFile.exists()) {
            return scaleFile;
        }

        ImgUtil.scale(cover, scaleFile, w, h, null);
        return scaleFile;
    }

}