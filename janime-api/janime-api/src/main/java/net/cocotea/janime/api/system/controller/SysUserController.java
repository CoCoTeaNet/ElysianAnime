package net.cocotea.janime.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.fastjson.JSONObject;
import net.cocotea.janime.api.system.model.dto.SysLoginUserUpdateDTO;
import net.cocotea.janime.api.system.model.dto.SysUserAddDTO;
import net.cocotea.janime.api.system.model.dto.SysUserPageDTO;
import net.cocotea.janime.api.system.model.dto.SysUserUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysUserVO;
import net.cocotea.janime.api.system.service.SysUserService;
import net.cocotea.janime.common.annotation.LogPersistence;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.ApiResult;
import net.cocotea.janime.common.model.BusinessException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;

@RequestMapping("/system/user")
@RestController
public class SysUserController {
    @Resource
    private SysUserService userService;

    /**
     * 添加用户信息。
     *
     * @param dto {@link SysUserAddDTO}
     * @return 返回操作结果，如果添加成功，返回成功的标志；如果添加失败，返回失败的标志。
     * @throws BusinessException 如果添加过程中出现业务逻辑错误，抛出此异常。
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/add")
    public ApiResult<Boolean> add(@Valid @RequestBody SysUserAddDTO dto) throws BusinessException {
        boolean b = userService.add(dto);
        return ApiResult.ok(b);
    }

    /**
     * 更新用户信息。
     *
     * @param param {@link SysUserUpdateDTO}
     * @return 返回操作结果，如果更新成功，返回成功的标志；如果更新失败，返回失败的标志。
     * @throws BusinessException 在更新过程中若出现业务逻辑错误，则抛出此异常。
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public ApiResult<Boolean> update(@Valid @RequestBody SysUserUpdateDTO param) throws BusinessException {
        boolean b = userService.update(param);
        return ApiResult.ok(b);
    }

    /**
     * 删除指定ID的用户信息。
     *
     * @param id 表示待删除用户的唯一标识符。
     * @return 返回操作结果，如果删除成功，返回包含成功状态的API结果；否则返回失败状态的API结果。
     * @throws BusinessException 在删除过程中，如果遇到业务逻辑错误，将会抛出此异常。
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/delete/{id}")
    public ApiResult<Boolean> delete(@PathVariable BigInteger id) throws BusinessException {
        boolean b = userService.delete(id);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除用户信息。
     *
     * @param idList 请求体中的JSON数组，包含了一组待删除用户的唯一标识符列表，
     *              每个元素均为用户的ID。
     * @return 返回操作结果，如果批量删除成功，则返回包含成功状态的API结果；否则返回失败状态的API结果。
     * @throws BusinessException 在执行批量删除过程中，如果遇到业务逻辑错误，将抛出此异常。
     */
    @LogPersistence
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/deleteBatch")
    public ApiResult<Boolean> deleteBatch(@RequestBody List<BigInteger> idList) throws BusinessException {
        boolean b = userService.deleteBatch(idList);
        return ApiResult.ok(b);
    }

    /**
     * 分页查询用户信息列表。
     *
     * @param param {@link SysUserPageDTO} 请求体中的JSON对象，包含了分页查询所需的参数，
     *              例如页码、每页数量以及其他可能的过滤条件等，并且该对象需符合验证规则。
     * @return 返回包含查询结果的API响应，成功时返回类型为{@link ApiPage<SysUserVO>}的对象，
     *         其中包含了满足查询条件的用户信息集合以及分页相关信息；若查询失败，则返回错误状态的API结果。
     * @throws BusinessException 在处理分页查询请求时遇到业务逻辑错误，将抛出此异常。
     */
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    @PostMapping("/listByPage")
    public ApiResult<ApiPage<SysUserVO>> listByPage(@Valid @RequestBody SysUserPageDTO param) throws BusinessException {
        ApiPage<SysUserVO> list = userService.listByPage(param);
        return ApiResult.ok(list);
    }

    /**
     * 获取用户详细信息。
     *
     * @return 返回包含用户详细信息的API响应，成功时返回类型为{@link SysUserVO}的对象，
     *         其中包含了用户的完整详情；若获取失败，则返回错误状态的API结果。
     */
    @GetMapping("/getDetail")
    public ApiResult<SysUserVO> getDetail() {
        SysUserVO vo = userService.getDetail();
        return ApiResult.ok(vo);
    }

    /**
     * 根据登录用户信息更新用户数据。
     *
     * @param param {@link SysLoginUserUpdateDTO} 请求体中的JSON对象，包含了需要更新的用户信息，
     *              并且该对象需符合验证规则。
     * @return 返回操作结果的API响应，若更新成功返回true，否则返回false。
     */
    @PostMapping("/updateByUser")
    public ApiResult<Boolean> updateByUser(@Valid @RequestBody SysLoginUserUpdateDTO param) {
        boolean b = userService.updateByUser(param);
        return ApiResult.ok(b);
    }

    /**
     * 处理修改密码请求。
     *
     * @param obj 请求体中的JSON对象，包含以下字段：
     *            - oldPassword：当前用户的旧密码，类型为字符串（String）。
     *            - newPassword：用户希望设置的新密码，类型为字符串（String）。
     * @return 返回操作结果的API响应，若密码修改成功返回true，否则返回false。
     * @throws BusinessException 当处理密码修改过程中遇到业务逻辑错误时抛出此异常。
     */
    @PostMapping("/doModifyPassword")
    public ApiResult<Boolean> doModifyPassword(@RequestBody JSONObject obj) throws BusinessException {
        boolean r = userService.doModifyPassword(obj.getString("oldPassword"), obj.getString("newPassword"));
        return ApiResult.ok(r);
    }
}
