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
import net.cocotea.janime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.janime.api.anime.service.AniOpusService;
import net.cocotea.janime.api.anime.service.AniSpiderService;
import net.cocotea.janime.common.annotation.LogPersistence;
import net.cocotea.janime.common.enums.ReadStatusEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 动漫作品接口
 */
@Validated
@RequestMapping("/anime/opus")
@RestController
public class AniOpusController {
    @Resource
    private AniOpusService aniOpusService;

    @Resource
    private AniSpiderService aniSpiderService;

    /**
     * 上传作品资源
     *
     * @param opusId        作品ID
     * @param multipartFile 文件流（multipart/form-data）
     * @return 路径
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/{opusId}/uploadRes")
    public ApiResult<String> uploadRes(@PathVariable("opusId") BigInteger opusId,
                                       @RequestParam("file") MultipartFile multipartFile) throws BusinessException {
        String s = aniOpusService.uploadRes(opusId, multipartFile);
        return ApiResult.ok(s);
    }

    /**
     * 添加作品信息,来源是Bangumi
     *
     * @param obj
     * @return {@link ApiResult}
     */
    @PostMapping("/addAcgOpusByBgmUrl")
    public ApiResult<?> addAcgOpusByBgmUrl(@RequestBody JSONObject obj) throws BusinessException {
        String bgmUrl = obj.getString("bgmUrl");
        if (StrUtil.isBlank(bgmUrl)) {
            throw new BusinessException("bgmUrl is empty");
        }
        Integer isCover = obj.getInteger("isCover");
        boolean b = aniSpiderService.addAniOpusByBgmUrl(bgmUrl, isCover);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("add")
    public ApiResult<?> add(@Valid @RequestBody AniOpusAddDTO param) throws BusinessException {
        boolean b = aniOpusService.add(param);
        return ApiResult.flag(b);
    }

    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/deleteBatch")
    public ApiResult<?> deleteBatch(@RequestBody List<BigInteger> param) throws BusinessException {
        boolean b = aniOpusService.deleteBatch(param);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public ApiResult<?> update(@Valid @RequestBody AniOpusUpdateDTO param) throws BusinessException {
        boolean b = aniOpusService.update(param);
        return ApiResult.flag(b);
    }

    @SaCheckRole(value = {"role:super:admin", "role:simple:admin", "bangumi:rss:subscriber"}, mode = SaMode.OR)
    @PostMapping("/listByPage")
    public ApiResult<?> listByPage(@Valid @RequestBody AniOpusPageDTO param) throws BusinessException {
        ApiPage<AniOpusVO> r = aniOpusService.listByPage(param);
        return ApiResult.ok(r);
    }

    @PostMapping("/listByUser")
    public ApiResult<?> listByUser(@Valid @RequestBody AniOpusHomeDTO homeDTO) throws BusinessException {
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

    @GetMapping("/getOpusMedia/{opusId}")
    public ApiResult<?> getOpusMedia(@PathVariable("opusId") BigInteger opusId) throws BusinessException {
        return ApiResult.ok(aniOpusService.getOpusMedia(opusId));
    }

    @GetMapping("/media/{opusId}")
    public void getMedia(@PathVariable("opusId") BigInteger opusId, @RequestParam("resName") String resName,
                         HttpServletRequest request, HttpServletResponse response) throws BusinessException, IOException, ServletException {
        aniOpusService.getMedia(opusId, resName, request, response);
    }

    @GetMapping("/cover")
    public void getCover(@RequestParam("resName") String resName, HttpServletResponse response) throws BusinessException, IOException {
        aniOpusService.getCover(resName, response);
    }

}
