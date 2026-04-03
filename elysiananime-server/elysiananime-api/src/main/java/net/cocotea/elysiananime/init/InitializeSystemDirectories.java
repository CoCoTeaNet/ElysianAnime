package net.cocotea.elysiananime.init;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.elysiananime.properties.FileProp;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import java.io.File;

/**
 * 初始化系统目录
 *
 * @author CoCoTea
 * @since 3.0.0
 * @date 2026-04-03
 */
@Slf4j
@Component
public final class InitializeSystemDirectories {

    @Inject
    private FileProp fileProp;

    @Init
    public void init() {
        log.info("init >>> 初始化系统目录");
        File file = FileUtil.file(fileProp.getDefaultSavePath());
        if (!file.exists()) {
            FileUtil.mkdir(file);
        }
        log.info("init >>> 默认文件保存的位置创建创建完成：{}", file.getPath());

        file = FileUtil.file(fileProp.getAvatarPath());
        if (!file.exists()) {
            FileUtil.mkdir(file);
        }
        log.info("init >>> 头像保存的位置创建完成：{}", file.getPath());

        file = FileUtil.file(fileProp.getBackgroundPath());
        if (!file.exists()) {
            FileUtil.mkdir(file);
        }
        log.info("init >>> 背景保存的位置创建完成：{}", file.getPath());

        file = FileUtil.file(fileProp.getAnimationCoverSavePath());
        if (!file.exists()) {
            FileUtil.mkdir(file);
        }
        log.info("init >>> 动画封面保存的位置创建完成：{}", file.getPath());

        file = FileUtil.file(fileProp.getAnimationSavePath());
        if (!file.exists()) {
            FileUtil.mkdir(file);
        }
        log.info("init >>> 动画保存的位置创建完成：{}", file.getPath());
    }

}
