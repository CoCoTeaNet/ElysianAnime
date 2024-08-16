package net.cocotea.janime.common.util;

import cn.hutool.core.text.CharPool;

import java.io.*;
import java.time.LocalDateTime;

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

}
