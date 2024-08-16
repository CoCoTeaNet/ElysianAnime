package net.cocotea.janime.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.janime.common.constant.CharConst;
import net.cocotea.janime.common.model.FileInfo;
import net.cocotea.janime.properties.FileProp;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Component
public class ResUtils {
    private static final Logger logger = LoggerFactory.getLogger(ResUtils.class);

    @Inject
    private FileProp fileProp;

    /**
     * 保存上传动漫资源
     *
     * @param opusName 作品名称
     * @return {@link FileInfo}
     */
    public FileInfo saveRes(String opusName, String originalFilename) {
        // 保存目录
        String saveDir = findASS(2) + opusName + CharConst.LEFT_LINE;
        // 文件信息
        return new FileInfo().setFileName(originalFilename).setFileDir(saveDir).setRealPath(saveDir + originalFilename);
    }

    /**
     * 查找可用的储存空间
     * <p>ASS: Available Storage Space</p>
     *
     * @param type 类型：1封面 2动漫资源
     * @return 储存空间目录
     */
    public String findASS(int type) {
        if (type == 1) {
            String savePath = fileProp.getAnimationCoverSavePath();
            String[] paths = savePath.split(CharConst.COMMA);
            return paths[0];
        } else if (type == 2) {
            String savePath = fileProp.getAnimationSavePath();
            String[] paths = savePath.split(CharConst.COMMA);
            return paths[0];
        } else {
            throw new RuntimeException("传参类型不在范围！");
        }
    }

    /**
     * 查找媒体资源所在目录
     *
     * @param opusName 作品名称
     * @return 如媒体存在就返回路径，否则返回null
     */
    public String findMediaDir(String opusName) {
        String[] paths = fileProp.getAnimationSavePath().split(CharConst.COMMA);
        String existPath = null;
        for (String path : paths) {
            String dir = path + opusName;
            if (FileUtil.exist(dir)) {
                existPath = dir;
                break;
            }
        }
        return existPath;
    }

    /**
     * 获取作品名称所在的真实路径
     *
     * @param opusName 作品名称
     * @param resName  媒体资源名称
     * @return 如媒体资源存在就返回文件 {@link File}
     */
    public File findResource(String opusName, String resName) {
        String mediaDir = findMediaDir(opusName);
        if (StrUtil.isNotBlank(mediaDir)) {
            String resourcePath = mediaDir + CharConst.LEFT_LINE + resName;
            if (FileUtil.exist(resourcePath)) {
                return FileUtil.file(resourcePath);
            }
        }
        return null;
    }

    /**
     * 判断资源是否存在（仅用于保存资源）
     *
     * @param opusName 作品名称
     * @param resName  资源名称
     * @return 存在True
     */
    public boolean exist(String opusName, String resName) {
        String path = findASS(2);
        if (StrUtil.isBlank(path)) {
            throw new NullPointerException("查找不到可用空间");
        }
        String dir = path + opusName;
        if (FileUtil.exist(dir)) {
            String resPath = dir + CharConst.LEFT_LINE + resName;
            return FileUtil.exist(resPath);
        } else {
            logger.warn("查找不到作品目录，保存空间：{}，作品名称：{}", path, opusName);
            return false;
        }
    }

    /**
     * 判断资源是否存在（仅用于保存资源）
     *
     * @param opusName 作品名称
     * @return 存在True
     */
    public boolean existDir(String opusName) {
        String path = findASS(2);
        if (StrUtil.isBlank(path)) {
            throw new NullPointerException("查找不到可用空间");
        }
        String dir = path + opusName;
        return FileUtil.exist(dir);
    }

}
