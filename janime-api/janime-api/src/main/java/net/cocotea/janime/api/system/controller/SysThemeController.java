package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import net.cocotea.janime.api.system.model.dto.SysThemeUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysThemeVO;
import net.cocotea.janime.api.system.service.SysThemeService;
import net.cocotea.janime.common.model.ApiResult;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Validated;

/**
 * 系统主题接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Mapping("/system/theme")
@Controller
public class SysThemeController {
    @Inject
    private SysThemeService sysThemeService;

    /**
     * 更新用户主题信息
     *
     * @param updateDTO {@link SysThemeUpdateDTO}
     * @return 成功返回TRUE
     */
    @SaCheckLogin
    @Post
    @Mapping("/updateByUser")
    public ApiResult<Boolean> updateByUser(@Validated @Body SysThemeUpdateDTO updateDTO) {
        boolean b = sysThemeService.updateByUser(updateDTO);
        return ApiResult.ok(b);
    }

    /**
     * 获取用户主题信息
     *
     * @return {@link SysThemeVO}
     */
    @SaCheckLogin
    @Get
    @Mapping("/loadByUser")
    public ApiResult<SysThemeVO> loadByUser() {
        SysThemeVO b = sysThemeService.loadByUser();
        return ApiResult.ok(b);
    }
}
