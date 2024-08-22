package net.cocotea.janime.api.system.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import net.cocotea.janime.api.system.model.dto.SysFileAddDTO;
import net.cocotea.janime.api.system.model.dto.SysFilePageDTO;
import net.cocotea.janime.api.system.model.dto.SysFileUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysFileVO;
import net.cocotea.janime.api.system.service.SysFileService;
import net.cocotea.janime.api.system.service.SysUserService;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.util.FileUploadUtils;
import net.cocotea.janime.properties.FileProp;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

/**
 * 系统文件管理接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Mapping("/system/file")
@Controller
public class SysFileController {
    private static final Logger logger = LoggerFactory.getLogger(SysFileController.class);

    @Inject
    private FileProp fileProp;

    @Inject
    private SysFileService sysFileService;

    @Inject
    private SysUserService sysUserService;

    /**
     * 系统文件上传
     *
     * @param uploadedFile {@link UploadedFile}
     * @return true表示成功
     */
    @Post
    @Mapping("/upload")
    public ApiResult<Boolean> upload(@Param("file") UploadedFile uploadedFile) throws BusinessException, IOException {
        // 过滤js，html，css等语言文件
        filter(uploadedFile);
        String fileName = uploadedFile.getName();
        String suffix = uploadedFile.getExtension();
        long contentSize = uploadedFile.getContentSize();
        String realPath = FileUploadUtils.createRealPath(fileProp.getDefaultSavePath(), fileName, suffix);
        uploadedFile.transferTo(new File(realPath));
        SysFileAddDTO fileAddDTO = new SysFileAddDTO()
                .setFileName(fileName)
                .setFileSuffix(suffix)
                .setFileSize(BigInteger.valueOf(contentSize))
                .setRealPath(realPath);
        sysFileService.add(fileAddDTO);
        return ApiResult.ok(true);
    }

    /**
     * 系统文件下载
     *
     * @param fileId 文件ID
     * @return {@link DownloadedFile}
     */
    @Get
    @Mapping("/download/{fileId}")
    public DownloadedFile download(@Path("fileId") BigInteger fileId) throws BusinessException, IOException {
        SysFileVO sysFileVO = sysFileService.getUserFile(fileId);
        //输出的文件名，可以自己指定
        InputStream stream = new FileInputStream(sysFileVO.getRealPath());
        return new DownloadedFile(ContentType.OCTET_STREAM.getValue(), stream, sysFileVO.getFileName());
    }

    /**
     * 系统文件批量删除
     *
     * @param param 文件ID列表
     * @return true表示成功
     */
    @Post
    @Mapping("/deleteBatch")
    public ApiResult<Boolean> deleteBatch(@Body List<BigInteger> param) throws BusinessException {
        boolean b = sysFileService.deleteBatch(param);
        return ApiResult.ok(b);
    }

    /**
     * 系统文件信息更新
     *
     * @param sysFileUpdateDTO {@link SysFileUpdateDTO}
     * @return true表示成功
     */
    @Post
    @Mapping("/update")
    public ApiResult<Boolean> update(@Validated @Body SysFileUpdateDTO sysFileUpdateDTO) throws BusinessException {
        boolean b = sysFileService.update(sysFileUpdateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 系统文件分页列表
     *
     * @param dto {@link SysFilePageDTO}
     * @return {@link ApiPage<SysFileVO>}
     */
    @Post
    @Mapping("/listByPage")
    public ApiResult<ApiPage<SysFileVO>> listByPage(@Validated @Body SysFilePageDTO dto) throws BusinessException {
        dto.setIsDeleted(IsEnum.N.getCode());
        ApiPage<SysFileVO> r = sysFileService.listByPage(dto);
        return ApiResult.ok(r);
    }

    /**
     * 系统回收站文件分页列表
     *
     * @param sysFilePageDTO {@link SysFilePageDTO}
     * @return {@link ApiPage<SysFileVO>}
     */
    @Post
    @Mapping("/recycleBinPage")
    public ApiResult<ApiPage<SysFileVO>> recycleBinPage(@Validated @Body SysFilePageDTO sysFilePageDTO) {
        ApiPage<SysFileVO> r = sysFileService.recycleBinPage(sysFilePageDTO);
        return ApiResult.ok(r);
    }

    /**
     * 系统回收站文件批量删除
     *
     * @param ids 文件ID集合
     * @return 成功返回true
     */
    @Post
    @Mapping("/recycleBin/deleteBatch")
    public ApiResult<Boolean> recycleBinDeleteBatch(@Validated @Body List<BigInteger> ids) {
        boolean recycled = sysFileService.recycleBinDeleteBatch(ids);
        return ApiResult.ok(recycled);
    }

    /**
     * 系统回收站文件批量恢复
     *
     * @param ids 文件ID集合
     * @return 成功返回true
     */
    @Post
    @Mapping("/recycleBin/recoveryBatch")
    public ApiResult<Boolean> recoveryBatch(@Validated @Body List<BigInteger> ids) {
        boolean recoveryBatch = sysFileService.recoveryBatch(ids);
        return ApiResult.ok(recoveryBatch);
    }

    /**
     * 系统用户头像上传
     *
     * @param uploadedFile {@link UploadedFile}
     * @return 成功返回true
     */
    @Post
    @Mapping("/avatar/upload")
    public ApiResult<Boolean> uploadAvatar(@Param("file") UploadedFile uploadedFile) throws BusinessException, IOException {
        if (StrUtil.isBlank(fileProp.getAvatarPath())) {
            throw new BusinessException("未配置相关信息");
        }
        filter(uploadedFile);
        String saveName = IdUtil.objectId() + CharPool.UNDERLINE + uploadedFile.getName();
        String fullPath = fileProp.getAvatarPath() + saveName;
        File file = new File(fullPath);
        if (!file.exists()) {
            FileUtil.mkdir(fileProp.getAvatarPath());
        }
        uploadedFile.transferTo(file);
        sysUserService.doModifyAvatar(saveName);
        return ApiResult.ok(true);
    }

    /**
     * 系统用户头像文件获取
     *
     * @param avatar 头像文件名称
     */
    @Get
    @Mapping("/getAvatar")
    public void getAvatar(@Param("avatar") String avatar, Context context) throws BusinessException, IOException {
        String fullPath = fileProp.getAvatarPath() + avatar;
        File file = FileUtil.file(fullPath);
        if (file.exists()) {
            FileUtil.writeToStream(file, context.outputStream());
        } else {
            throw new BusinessException("文件不存在");
        }
    }

    private void filter(UploadedFile uploadedFile) throws BusinessException {
        if (uploadedFile != null) {
            String extension = uploadedFile.getExtension();
            if (StrUtil.isBlank(extension)) {
                throw new BusinessException("未知文件格式");
            } else {
                boolean flag = fileProp.getNotSupportFiletype().contains(extension);
                if (flag) {
                    throw new BusinessException("该文件格式不支持上传");
                }
            }
        } else {
            throw new BusinessException("文件名为空");
        }
    }

}
