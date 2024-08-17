package net.cocotea.janime.api.anime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.janime.api.anime.model.dto.*;
import net.cocotea.janime.api.anime.model.po.AniOpus;
import net.cocotea.janime.api.anime.model.po.AniTag;
import net.cocotea.janime.api.anime.model.po.AniUserOpus;
import net.cocotea.janime.api.anime.model.vo.AniOpusHomeVO;
import net.cocotea.janime.api.anime.model.vo.AniOpusVO;
import net.cocotea.janime.api.anime.model.vo.AniVideoVO;
import net.cocotea.janime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.janime.api.system.service.SysNotifyService;
import net.cocotea.janime.api.anime.service.AniOpusService;
import net.cocotea.janime.api.anime.service.AniTagService;
import net.cocotea.janime.api.anime.service.AniUserOpusService;
import net.cocotea.janime.common.constant.NotifyConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.enums.LevelEnum;
import net.cocotea.janime.common.enums.ReadStatusEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.model.FileInfo;
import net.cocotea.janime.properties.FileProp;
import net.cocotea.janime.util.LoginUtils;
import net.cocotea.janime.util.ResUtils;
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
import java.util.stream.Collectors;

/**
 * 作品
 *
 * @author CoCoTea 572315466@qq.com
 * @since 1.2.1 2023-03-07
 */
@Component
public class AniOpusServiceImpl implements AniOpusService {
    private static final Logger logger = LoggerFactory.getLogger(AniOpusServiceImpl.class);

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
        Page<AniOpusHomeVO> page = lightDao.findPage(ApiPage.create(homeDTO), "ani_opus_listByUser", mapDTO, AniOpusHomeVO.class);
        return ApiPage.rest(page);
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
        Map<String, Object> beanDTO = BeanUtil.beanToMap(pageDTO);
        beanDTO.put("likeNameOriginal", pageDTO.getAniOpus().getNameOriginal());
        beanDTO.put("likeNameCn", pageDTO.getAniOpus().getNameCn());
        Page<AniOpusVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ani_opus_findList", beanDTO, AniOpusVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AniOpus aniOpus = new AniOpus().setId(id).setIsDeleted(IsEnum.N.getCode());
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
            vo.setReadingNum(1);
            vo.setReadingTime(BigInteger.valueOf(0));
            vo.setIsFollow(IsEnum.N.getCode());
            vo.setIsShare(IsEnum.N.getCode());
        }
        vo.setUserId(userOpus.getUserId());
        // 浏览本地资源
        String folder = resUtils.findMediaDir(aniOpus.getNameCn());
        List<AniVideoVO.Media> medias = Collections.emptyList();
        if (folder != null) {
            File[] files = FileUtil.ls(folder);
            medias = getMedias(files);
        }
        List<AniVideoVO.Media> mediaList = medias
                .stream()
                .sorted(Comparator.comparing(i -> Integer.parseInt(i.getEpisodes())))
                .collect(Collectors.toList());
        vo.setMediaList(mediaList);
        // 查找标签
        List<AniTag> tagList = aniTagService.findByOpusId(aniOpus.getId());
        vo.setAniTags(tagList);
        return vo;
    }

    private List<AniVideoVO.Media> getMedias(File[] files) {
        List<AniVideoVO.Media> mediaList = new ArrayList<>(files.length);
        for (File file : files) {
            if (!file.isDirectory()) {
                String[] split = file.getName().split("\\.");
                String suffix = split[split.length - 1];
                if (!fileProp.getMediaFileType().contains(suffix)) {
                    continue;
                }
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 0; i < split.length - 1; i++) {
                    nameBuilder.append(split[i]);
                }
                if (NumberUtil.isNumber(nameBuilder.toString())) {
                    AniVideoVO.Media media = new AniVideoVO
                            .Media()
                            .setEpisodes(nameBuilder.toString()).setMediaType(suffix);
                    mediaList.add(media);
                } else {
                    logger.warn("getMedias >>>>> [{}] is not number", nameBuilder);
                }
            }
        }
        return mediaList;
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
    public File getMedia(BigInteger opusId, String resName) throws BusinessException {
        AniOpus aniOpus = lightDao.load(new AniOpus().setId(opusId));
        if (aniOpus == null) {
            throw new BusinessException("作品不存在");
        }

        File resource = resUtils.findResource(aniOpus.getNameCn(), resName);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }

        if (!resource.exists()) {
            throw new BusinessException("文件不存在");
        }

        return resource;
    }

    @Override
    public File getCover(String resName) throws BusinessException, IOException {
        if (StrUtil.isBlank(resName)) {
            throw new BusinessException("资源不存在");
        }
        File file = FileUtil.file(fileProp.getAnimationCoverSavePath() + resName);
        if (file.exists()) {
            return file;
        } else {
            throw new BusinessException("文件不存在");
        }
    }

}