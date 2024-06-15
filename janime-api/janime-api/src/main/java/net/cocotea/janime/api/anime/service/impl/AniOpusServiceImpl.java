package net.cocotea.janime.api.anime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
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
import net.cocotea.janime.interceptor.NonStaticResourceHttpRequestHandler;
import net.cocotea.janime.properties.FileProp;
import net.cocotea.janime.util.LoginUtils;
import net.cocotea.janime.util.ResUtils;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 作品
 *
 * @author CoCoTea 572315466@qq.com
 * @since 1.2.1 2023-03-07
 */
@Service
public class AniOpusServiceImpl implements AniOpusService {
    private final Logger logger = LoggerFactory.getLogger(AniOpusServiceImpl.class);

    @Resource
    private NonStaticResourceHttpRequestHandler nonStaticResourceHttpRequestHandler;

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Resource
    private ResUtils resUtils;

    @Resource
    private SysNotifyService sysNotifyService;

    @Resource
    private AniUserOpusService aniUserOpusService;

    @Resource
    private AniTagService aniTagService;

    @Resource
    private FileProp fileProp;

    @Override
    public boolean update(AniOpus aniOpus) {
        Long count = sqlToyHelperDao.update(aniOpus);
        return count > 0;
    }

    @Override
    public AniOpus loadById(BigInteger id) throws BusinessException {
        if (id == null) {
            throw new BusinessException("主键为空");
        }
        LambdaQueryWrapper<AniOpus> queryWrapper = new LambdaQueryWrapper<>(AniOpus.class)
                .select()
                .eq(AniOpus::getId, id)
                .eq(AniOpus::getIsDeleted, IsEnum.N.getCode());
        return sqlToyHelperDao.findOne(queryWrapper);
    }

    @Override
    public AniOpusVO loadByName(String nameOriginal) throws BusinessException {
        if (StrUtil.isBlank(nameOriginal)) {
            throw new BusinessException("名称为空");
        }
        LambdaQueryWrapper<AniOpus> wrapper = new LambdaQueryWrapper<>(AniOpus.class)
                .select()
                .eq(AniOpus::getNameOriginal, nameOriginal);
        AniOpus aniOpus = sqlToyHelperDao.findOne(wrapper);
        return Convert.convert(AniOpusVO.class, aniOpus);
    }

    @Override
    public AniOpus loadByNameCn(String nameCn) throws BusinessException {
        if (StrUtil.isBlank(nameCn)) {
            throw new BusinessException("名称为空");
        }
        LambdaQueryWrapper<AniOpus> wrapper = new LambdaQueryWrapper<>(AniOpus.class)
                .select()
                .eq(AniOpus::getNameCn, nameCn);
        AniOpus aniOpus = sqlToyHelperDao.findOne(wrapper);
        return Convert.convert(AniOpus.class, aniOpus);
    }

    @Override
    public ApiPage<AniOpusHomeVO> listByUser(AniOpusHomeDTO homeDTO) {
        BigInteger loginId = LoginUtils.loginId();
        // 标签查询
        if (!homeDTO.getStates().isEmpty()) {
            List<BigInteger> opusIdsFromTag = aniTagService.findTagIds(homeDTO.getStates());
            if (!opusIdsFromTag.isEmpty()) {
                if (homeDTO.getOpusIds() == null) {
                    homeDTO.setOpusIds(opusIdsFromTag);
                } else {
                    homeDTO.getOpusIds().addAll(opusIdsFromTag);
                }
            }
        }
        // 作品主表查询
        Map<String, Object> mapDTO = BeanUtil.beanToMap(homeDTO);
        mapDTO.put("loginId", loginId);
        Page<AniOpusHomeVO> page = sqlToyHelperDao.findPageBySql(homeDTO, "ani_opus_listByUser", mapDTO, AniOpusHomeVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean add(AniOpusAddDTO param) {
        AniOpus aniOpus = Convert.convert(AniOpus.class, param);
        Object save = sqlToyHelperDao.save(aniOpus);
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
    public boolean update(AniOpusUpdateDTO updateDTO) {
        AniOpus aniOpus = Convert.convert(AniOpus.class, updateDTO);
        Long count = sqlToyHelperDao.update(aniOpus);
        return count > 0;
    }

    @Override
    public ApiPage<AniOpusVO> listByPage(AniOpusPageDTO pageDTO) {
        LambdaQueryWrapper<AniOpus> wrapper = Wrappers.lambdaWrapper(AniOpus.class)
                .eq(AniOpus::getIsDeleted, IsEnum.N.getCode())
                .eq(AniOpus::getRssStatus, pageDTO.getAniOpus().getRssStatus())
                .eq(AniOpus::getHasResource, pageDTO.getAniOpus().getHasResource())
                .like(AniOpus::getNameOriginal, pageDTO.getAniOpus().getNameOriginal())
                .like(AniOpus::getNameCn, pageDTO.getAniOpus().getNameCn())
                .orderByDesc(AniOpus::getId);
        Page<AniOpus> page = sqlToyHelperDao.findPage(wrapper, pageDTO);
        return ApiPage.rest(page, AniOpusVO.class);
    }

    @Override
    public boolean delete(BigInteger id) {
        AniOpus aniOpus = new AniOpus().setId(id).setIsDeleted(IsEnum.N.getCode());
        return sqlToyHelperDao.update(aniOpus) > 0;
    }

    @Override
    public AniVideoVO getOpusMedia(BigInteger opusId) {
        AniOpus aniOpus = sqlToyHelperDao.loadEntity(AniOpus.class, new EntityQuery().names("ID").values(opusId));
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
        LambdaQueryWrapper<AniOpus> wrapper = new LambdaQueryWrapper<>(AniOpus.class)
                .select()
                .eq(AniOpus::getIsDeleted, IsEnum.N.getCode())
                .eq(AniOpus::getRssStatus, rssStatus);
        return sqlToyHelperDao.findList(wrapper);
    }

    @Override
    public String uploadRes(BigInteger opusId, MultipartFile multipartFile) throws BusinessException {
        if (multipartFile.getOriginalFilename() != null) {
            String[] split = multipartFile.getOriginalFilename().split("\\.");
            String fileType = split[split.length - 1];
            if (fileProp.getMediaFileType().contains(fileType)) {
                AniOpus opus = sqlToyHelperDao.load(new AniOpus().setId(opusId));
                FileInfo fileInfo = resUtils.saveRes(multipartFile, opus.getNameCn());
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
                return fileInfo.getFileName();
            } else {
                throw new BusinessException("目前只支持格式：" + fileProp.getMediaFileType());
            }
        } else {
            throw new BusinessException("文件名为空.");
        }
    }

    @Override
    public void getMedia(BigInteger opusId, String resName, HttpServletRequest request, HttpServletResponse response) throws BusinessException, IOException, ServletException {
        AniOpus aniOpus = sqlToyHelperDao.load(new AniOpus().setId(opusId));
        if (aniOpus == null) {
            throw new BusinessException("作品不存在");
        }
        File resource = resUtils.findResource(aniOpus.getNameCn(), resName);
        if (resource.exists()) {
            String mimeType = Files.probeContentType(Paths.get(resource.getPath()));
            if (StrUtil.isNotEmpty(mimeType)) {
                response.setContentType(mimeType);
            }
            request.setAttribute(NonStaticResourceHttpRequestHandler.ATTR_FILE, resource.toPath());
            response.setContentType(Files.probeContentType(resource.toPath()));
            nonStaticResourceHttpRequestHandler.handleRequest(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            throw new BusinessException("文件不存在");
        }
    }

    @Override
    public void getCover(String resName, HttpServletResponse response) throws BusinessException, IOException {
        if (StrUtil.isBlank(resName)) {
            throw new BusinessException("资源不存在");
        }
        File file = FileUtil.file(fileProp.getAnimationCoverSavePath() + resName);
        if (file.exists()) {
            FileUtil.writeToStream(file, response.getOutputStream());
        } else {
            throw new BusinessException("文件不存在");
        }
    }
}