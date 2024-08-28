package net.cocotea.elysiananime.api.system.controller;

import net.cocotea.elysiananime.api.system.model.vo.SysNotifyVO;
import net.cocotea.elysiananime.api.system.service.SysNotifyService;
import net.cocotea.elysiananime.common.model.ApiResult;
import org.noear.solon.annotation.*;

import java.math.BigInteger;
import java.util.List;

@Mapping("/anime/notify")
@Controller
public class SysNotifyController {

    @Inject
    private SysNotifyService sysNotifyService;

    @Get
    @Mapping("/listByType")
    public ApiResult<?> listByType(@Param("type") String type) {
        List<SysNotifyVO> vos = sysNotifyService.listByType(type);
        return ApiResult.ok(vos);
    }

    @Post @Mapping("/read/{id}")
    public ApiResult<?> read(@Path("id") BigInteger id) {
        sysNotifyService.read(id);
        return ApiResult.ok();
    }

}
