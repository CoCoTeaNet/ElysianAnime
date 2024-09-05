package net.cocotea.elysiananime.api.system.service;

import net.cocotea.elysiananime.api.system.model.dto.SysThemeUpdateDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysThemeVO;

/**
 * 系统主题表
 *
 * @author CoCoTea 572315466@qq.com
 * @since 1.2.4 2023-02-25
 */
public interface SysThemeService {
    boolean updateByUser(SysThemeUpdateDTO param);

    SysThemeVO loadByUser();
}