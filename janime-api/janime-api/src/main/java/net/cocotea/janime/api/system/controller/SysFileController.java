package net.cocotea.janime.api.system.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.janime.api.system.model.dto.SysFileAddDTO;
import net.cocotea.janime.api.system.model.dto.SysFilePageDTO;
import net.cocotea.janime.api.system.model.dto.SysFileUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysFileVO;
import net.cocotea.janime.api.system.service.SysFileService;
import net.cocotea.janime.api.system.service.SysUserService;
import net.cocotea.janime.common.constant.CharConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.model.FileInfo;
import net.cocotea.janime.common.util.FileUploadUtils;
import net.cocotea.janime.properties.FileProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Validated
@RequestMapping("/system/file")
@RestController
public class SysFileController {
    private final Logger logger = LoggerFactory.getLogger(SysFileController.class);

    @Resource
    private FileProp fileProp;

    @Resource
    private SysFileService sysFileService;

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/upload")
    public ApiResult<?> upload(@RequestParam("file") MultipartFile multipartFile) throws BusinessException {
        // 过滤js，html，css等语言文件
        filter(multipartFile);
        FileInfo fileInfo = FileUploadUtils.saveMultipartFile(multipartFile, fileProp.getDefaultSavePath());
        SysFileAddDTO fileAddDTO = new SysFileAddDTO()
                .setFileName(fileInfo.getFileName())
                .setFileSuffix(fileInfo.getFileSuffix())
                .setFileSize(BigInteger.valueOf(fileInfo.getFileSize()))
                .setRealPath(fileInfo.getFileBasePath());
        sysFileService.add(fileAddDTO);
        logger.debug("文件保存信息: {}", fileInfo);
        return ApiResult.ok();
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable("fileId") BigInteger fileId) throws BusinessException, IOException {
        SysFileVO sysFileVO = sysFileService.getUserFile(fileId);
        return getResponseEntity(sysFileVO.getRealPath(), sysFileVO.getFileName());
    }

    @PostMapping("/deleteBatch")
    public ApiResult<?> deleteBatch(@RequestBody List<BigInteger> param) throws BusinessException {
        boolean b = sysFileService.deleteBatch(param);
        return ApiResult.flag(b);
    }

    @PostMapping("/update")
    public ApiResult<?> update(@Valid @RequestBody SysFileUpdateDTO sysFileUpdateDTO) throws BusinessException {
        boolean b = sysFileService.update(sysFileUpdateDTO);
        return ApiResult.flag(b);
    }

    @PostMapping("/listByPage")
    public ApiResult<?> listByPage(@Valid @RequestBody SysFilePageDTO dto) throws BusinessException {
        dto.setIsDeleted(IsEnum.N.getCode());
        ApiPage<SysFileVO> r = sysFileService.listByPage(dto);
        return ApiResult.ok(r);
    }

    @PostMapping("/recycleBinPage")
    public ApiResult<?> recycleBinPage(@Valid @RequestBody SysFilePageDTO param) {
        ApiPage<SysFileVO> r = sysFileService.recycleBinPage(param);
        return ApiResult.ok(r);
    }

    @PostMapping("/recycleBin/deleteBatch")
    public ApiResult<?> recycleBinDeleteBatch(@Valid @RequestBody List<BigInteger> param) {
        return ApiResult.flag(sysFileService.recycleBinDeleteBatch(param));
    }

    @PostMapping("/recycleBin/recoveryBatch")
    public ApiResult<?> recoveryBatch(@Valid @RequestBody List<BigInteger> param) {
        return ApiResult.flag(sysFileService.recoveryBatch(param));
    }

    @PostMapping("/avatar/upload")
    public ApiResult<?> uploadAvatar(@RequestParam("file") MultipartFile multipartFile) throws BusinessException, IOException {
        if (StrUtil.isBlank(fileProp.getAvatarPath())) {
            throw new BusinessException("未配置相关信息");
        }
        filter(multipartFile);
        String saveName = IdUtil.objectId() + CharConst.UNDERLINE + multipartFile.getOriginalFilename();
        String fullPath = fileProp.getAvatarPath() + saveName;
        File file = new File(fullPath);
        if (!file.exists()) {
            FileUtil.mkdir(fileProp.getAvatarPath());
        }
        multipartFile.transferTo(file);
        sysUserService.doModifyAvatar(saveName);
        return ApiResult.ok();
    }

    @GetMapping("/getAvatar")
    public void getAvatar(@RequestParam("avatar") String avatar, HttpServletResponse response) throws BusinessException, IOException {
        String fullPath = fileProp.getAvatarPath() + avatar;
        File file = FileUtil.file(fullPath);
        if (file.exists()) {
            FileUtil.writeToStream(file, response.getOutputStream());
        } else {
            throw new BusinessException("文件不存在");
        }
    }

    @GetMapping("/getBackground")
    public void getBackground(@RequestParam(value = "resName", required = false) String resName, HttpServletResponse response) throws BusinessException, IOException {
        String fullPath = fileProp.getBackgroundPath();
        if (StrUtil.isNotBlank(resName)) {
            // 指定背景
            fullPath += resName;
            File file = FileUtil.file(fullPath);
            if (file.exists()) {
                FileUtil.writeToStream(file, response.getOutputStream());
            } else {
                throw new BusinessException("文件不存在");
            }
        } else {
            // 随机背景
            File file = FileUtil.file(fullPath);
            if (file.exists() && file.isDirectory()) {
                List<File> files = FileUtil.loopFiles(file);
                File random = files.get(RandomUtil.randomInt(0, files.size() - 1));
                FileUtil.writeToStream(random, response.getOutputStream());
            }
        }
    }

    private void filter(MultipartFile multipartFile) throws BusinessException {
        if (multipartFile.getOriginalFilename() != null) {
            String[] split = multipartFile.getOriginalFilename().split("\\.");
            String fileType = split[split.length - 1];
            if (StrUtil.isBlank(fileType)) {
                throw new BusinessException("未知文件格式");
            } else {
                boolean flag = fileProp.getNotSupportFiletype().contains(fileType);
                if (flag) {
                    throw new BusinessException("该文件格式不支持上传");
                }
            }
        } else {
            throw new BusinessException("文件名为空");
        }
    }

    private ResponseEntity<StreamingResponseBody> getResponseEntity(String filePath, String fileName) throws IOException {
        String fullPath = fileProp.getDefaultSavePath() + filePath;
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncodeUtil.encode(fileName) + "\"");
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(Paths.get(fullPath))));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out -> {
                    try (InputStream in = FileUtil.getInputStream(fullPath)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to write file to output stream", e);
                    }
                });
    }

}
