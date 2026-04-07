package net.cocotea.elysiananime.common.util;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import net.cocotea.elysiananime.common.model.BusinessException;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传工具类
 *
 * @author jwss
 * @date 2022-4-9 23:34:04
 */
public class FileUploadUtils {

    /**
     * 创建文件储存真实路径
     *
     * @param basePath 基础目录
     * @param saveName 文件名
     * @param suffix   文件后缀
     * @return 真实路径
     */
    public static String createRealPath(String basePath, String saveName, String suffix) {
        if (saveName == null || saveName.trim().isEmpty()) {
            throw new RuntimeException("文件名为空");
        }
        // 如果目录不存在则新建
        String baseDir = createBaseDir();
        File file = new File(basePath + baseDir);
        if (!file.exists()) {
            boolean mkdir = file.mkdirs();
            System.out.println("mkdir >>>>> flag=" + mkdir + ",path=" + file.getPath());
        }
        // 创建文件真实路径
        return file.getPath() + saveName;
    }

    /**
     * 创建基础存放目录
     *
     * @return 基础存放目录
     */
    public static String createBaseDir() {
        LocalDateTime now = LocalDateTime.now();
        return now.getYear() + String.valueOf(CharPool.SLASH) +
                now.getMonthValue() + CharPool.SLASH +
                now.getDayOfMonth() + CharPool.SLASH;
    }

    public static void filter(String originalFilename, String supportFiletype) throws BusinessException {
        if (originalFilename != null) {
            String[] split = originalFilename.split("\\.");
            String fileType = split[split.length - 1];
            if (StrUtil.isBlank(fileType)) {
                throw new BusinessException("未知文件格式");
            } else {
                boolean supportFlag = Arrays.stream(supportFiletype.split(StrPool.COMMA))
                        .toList()
                        .contains(fileType);
                Assert.isTrue(supportFlag, () -> new BusinessException("该文件格式不支持上传"));
            }
        } else {
            throw new BusinessException("文件名为空");
        }
    }

    /**
     * 验证头像文件
     * <p>
     * 仅支持jpg、jpeg、png、gif格式
     * @param file 文件
     * @throws BusinessException 验证失败
     */
    public static void validAvatar(File file) throws BusinessException, IOException {
        Assert.isFalse(file == null, () -> new BusinessException("空文件对象"));
        Assert.isTrue(file.exists(), () -> new BusinessException("文件不存在"));
        BufferedInputStream inputStream = FileUtil.getInputStream(file);
        String type = FileTypeUtil.getType(inputStream);
        inputStream.close();
        Assert.isFalse(type == null, () -> {
            FileUtil.del(file);
            return new BusinessException("未知文件格式");
        });
        Assert.isTrue(List.of("jpg", "jpeg", "png", "gif").contains(type), () -> {
            FileUtil.del(file);
            return new BusinessException("仅支持jpg、jpeg、png、gif格式");
        });
    }

}
