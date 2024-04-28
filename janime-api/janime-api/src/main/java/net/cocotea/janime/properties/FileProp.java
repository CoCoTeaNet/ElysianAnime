package net.cocotea.janime.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    @Value("${janime.file.default-path}")
    private String defaultSavePath;

    /**
     * 头像保存位置
     */
    @Value("${janime.file.avatar}")
    private String avatarPath;

    /**
     * 封面背景存放地址
     */
    @Value("${janime.file.background}")
    private String backgroundPath;

    /**
     * 动漫封面保存的位置
     */
    @Value("${janime.file.animation-cover-path}")
    private String animationCoverSavePath;

    /**
     * 动漫资源保存位置
     */
    @Value("${janime.file.animation-path}")
    private String animationSavePath;

    /**
     * 不支持上传的文件
     */
    @Value("${janime.file.not-support-filetype}")
    private String notSupportFiletype;

    /**
     * 媒体格式
     */
    @Value("${janime.file.media-filetype}")
    private String mediaFileType;

}
