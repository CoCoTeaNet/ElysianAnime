package net.cocotea.janime.api.anime.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import net.cocotea.janime.api.anime.model.dto.AniOpusAddDTO;
import net.cocotea.janime.api.anime.model.dto.AniOpusHomeDTO;
import net.cocotea.janime.api.anime.model.dto.AniOpusPageDTO;
import net.cocotea.janime.api.anime.model.dto.AniOpusUpdateDTO;
import net.cocotea.janime.api.anime.model.vo.AniOpusHomeVO;
import net.cocotea.janime.api.anime.model.vo.AniOpusVO;
import net.cocotea.janime.api.anime.service.AniOpusService;
import net.cocotea.janime.api.anime.service.AniSpiderService;
import net.cocotea.janime.common.annotation.LogPersistence;
import net.cocotea.janime.common.enums.ReadStatusEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.model.FileInfo;
import net.cocotea.janime.properties.FileProp;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 动漫作品接口
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
    public ApiResult<?> addAcgOpusByBgmUrl(@Body JSONObject obj) throws BusinessException {
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

    @Get
    @Mapping("/getOpusMedia/{opusId}")
    public ApiResult<?> getOpusMedia(@Path("opusId") BigInteger opusId) throws BusinessException {
        return ApiResult.ok(aniOpusService.getOpusMedia(opusId));
    }

    @Get
    @Mapping("/media/{opusId}")
    public DownloadedFile getMedia(@Path("opusId") BigInteger opusId, @Param("resName") String resName) throws BusinessException, IOException {
        File file = aniOpusService.getMedia(opusId, resName);
        DownloadedFile downloadedFile = new DownloadedFile(file);
        //不做为附件下载（按需配置）
        downloadedFile.asAttachment(false);
        return downloadedFile;
    }

    @Get
    @Mapping("/cover")
    public DownloadedFile getCover(@Param("resName") String resName) throws BusinessException, IOException {
        File cover = aniOpusService.getCover(resName);
        return new DownloadedFile(cover);
    }

}
