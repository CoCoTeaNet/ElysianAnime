package net.cocotea.janime.properties;

import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * 默认值配置项
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Component
public class FileProp {
    
    /**
     * 默认文件保存的位置
     */
    @Inject("${sra-admin.file.default-path}")
    private String defaultSavePath;

    /**
     * 头像保存位置
     */
    @Inject("${sra-admin.file.avatar}")
    private String avatarPath;

    /**
     * 不支持上传的文件
     */
    @Inject("${sra-admin.file.not-support-filetype}")
    private String notSupportFiletype;

    /**
     * 媒体格式
     */
    @Inject("${sra-admin.file.media-filetype}")
    private String mediaFileType;

    /**
     * 封面背景存放地址
     */
    @Inject("${janime.file.background}")
    private String backgroundPath;

    /**
     * 动漫封面保存的位置
     */
    @Inject("${janime.file.animation-cover-path}")
    private String animationCoverSavePath;

    /**
     * 动漫资源保存位置
     */
    @Inject("${janime.file.animation-path}")
    private String animationSavePath;
    
}
