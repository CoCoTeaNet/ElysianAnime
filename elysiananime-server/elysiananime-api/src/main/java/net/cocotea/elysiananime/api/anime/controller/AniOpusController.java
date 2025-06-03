package net.cocotea.elysiananime.api.anime.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import net.cocotea.elysiananime.api.anime.model.dto.AniOpusAddDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniOpusHomeDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniOpusPageDTO;
import net.cocotea.elysiananime.api.anime.model.dto.AniOpusUpdateDTO;
import net.cocotea.elysiananime.api.anime.model.vo.AniOpusHomeVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniOpusVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniVideoVO;
import net.cocotea.elysiananime.api.anime.service.AniOpusService;
import net.cocotea.elysiananime.api.anime.service.AniSpiderService;
import net.cocotea.elysiananime.common.annotation.LogPersistence;
import net.cocotea.elysiananime.common.enums.ReadStatusEnum;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.ApiResult;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.model.FileInfo;
import net.cocotea.elysiananime.properties.FileProp;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 番剧作品接口
 *
 * @author CoCoTea
 */
@Mapping("/anime/opus")
@Controller
public class AniOpusController {

    @Inject
    private FileProp fileProp;

    @Inject
    private AniOpusService aniOpusService;

    @Inject
    private AniSpiderService aniSpiderService;

    /**
     * 上传作品资源
     *
     * @param opusId        作品ID
     * @return 路径
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("/{opusId}/uploadRes")
    public ApiResult<String> uploadRes(@Path("opusId") BigInteger opusId,
                                       @Param(value = "file",required = true) UploadedFile uploadedFile) throws BusinessException, IOException {
        String fileExtension = uploadedFile.getExtension();
        String fileName = uploadedFile.getName();

        if (!fileProp.getMediaFileType().contains(fileExtension)) {
            throw new BusinessException("目前只支持格式：" + fileProp.getMediaFileType());
        }

        FileInfo fileInfo = aniOpusService.uploadRes(opusId, (fileName+fileExtension));
        uploadedFile.transferTo(new File(fileInfo.getRealPath()));

        return ApiResult.ok(fileInfo.getFileName());
    }

    /**
     * 添加作品信息,来源是Bangumi
     *
     * @param obj {bgmUrl："地址",isCover:"是否覆盖"}
     * @return {@link ApiResult}
     */
    @Post
    @Mapping("/addAcgOpusByBgmUrl")
    public ApiResult<?> addAniOpusByBgmUrl(@Body JSONObject obj) throws BusinessException {
        String bgmUrl = obj.getString("bgmUrl");
        if (StrUtil.isBlank(bgmUrl)) {
            throw new BusinessException("bgmUrl is empty");
        }
        Integer isCover = obj.getInteger("isCover");
        boolean b = aniSpiderService.addAniOpusByBgmUrl(bgmUrl, isCover);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Post
    @Mapping("add")
    public ApiResult<?> add(@Body AniOpusAddDTO param) throws BusinessException {
        boolean b = aniOpusService.add(param);
        return ApiResult.flag(b);
    }

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Mapping("/deleteBatch")
    public ApiResult<?> deleteBatch(@Body List<BigInteger> param) throws BusinessException {
        boolean b = aniOpusService.deleteBatch(param);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @Mapping("/update")
    public ApiResult<?> update(@Body AniOpusUpdateDTO param) throws BusinessException {
        boolean b = aniOpusService.update(param);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @Mapping("/listByPage")
    public ApiResult<?> listByPage(@Body AniOpusPageDTO param) throws BusinessException {
        ApiPage<AniOpusVO> r = aniOpusService.listByPage(param);
        return ApiResult.ok(r);
    }

    @Mapping("/listByUser")
    public ApiResult<?> listByUser(@Body AniOpusHomeDTO homeDTO) throws BusinessException {
        List<String> list = new ArrayList<>(homeDTO.getStatus().size());
        for (String status : homeDTO.getStatus()) {
            if (status.equals(ReadStatusEnum.NOT_READ.getAliseName())) {
                list.add(String.valueOf(ReadStatusEnum.NOT_READ.getCode()));
            } else if (status.equals(ReadStatusEnum.IS_READ.getAliseName())) {
                list.add(String.valueOf(ReadStatusEnum.IS_READ.getCode()));
            } else if (status.equals(ReadStatusEnum.READING.getAliseName())) {
                list.add(String.valueOf(ReadStatusEnum.READING.getCode()));
            }
        }
        homeDTO.setStatus(list);
        ApiPage<AniOpusHomeVO> r = aniOpusService.listByUser(homeDTO);
        return ApiResult.ok(r);
    }

    /**
     * 获取作品详情
     *
     * @param opusId 作品ID
     * @return {@link AniVideoVO}
     */
    @Get
    @Mapping("/getOpusMedia/{opusId}")
    public ApiResult<AniVideoVO> getOpusMedia(@Path("opusId") BigInteger opusId) throws BusinessException {
        return ApiResult.ok(aniOpusService.getOpusMedia(opusId));
    }

    /**
     * 获取资源媒体流
     *
     * @param opusId   作品ID
     * @param resName  资源名称
     */
    @Get
    @Mapping("/media/{opusId}")
    public DownloadedFile getMedia(@Path("opusId") BigInteger opusId, @Param(value = "resName", required = true) String resName) throws BusinessException, IOException {
        File file = aniOpusService.getMedia(opusId, resName);
        DownloadedFile downloadedFile = new DownloadedFile(file);
        //不做为附件下载（按需配置）
        downloadedFile.asAttachment(false);
        return downloadedFile;
    }

    @Get
    @Mapping("/cover")
    public DownloadedFile getCover(@Param("resName") String resName,
                                   @Param(required = false) Integer w,
                                   @Param(required = false) Integer h) throws BusinessException, IOException {
        File cover = aniOpusService.getCover(resName, w, h);
        return new DownloadedFile(cover);
    }

    @Get
    @Mapping("/getBackground")
    public DownloadedFile getBackground(@Param(value = "resName") String resName) throws BusinessException, FileNotFoundException {
        String fullPath = fileProp.getBackgroundPath();
        File bgFile = null;
        if (StrUtil.isNotBlank(resName)) {
            // 指定背景
            fullPath += resName;
            bgFile = FileUtil.file(fullPath);
            if (!bgFile.exists()) {
                throw new BusinessException("文件不存在");
            }
        } else {
            // 随机背景
            File file = FileUtil.file(fullPath);
            if (file.exists() && file.isDirectory()) {
                List<File> files = FileUtil.loopFiles(file);
                bgFile = files.get(RandomUtil.randomInt(0, files.size() - 1));
            }
        }
        assert bgFile != null;
        return new DownloadedFile(bgFile);
    }

}
