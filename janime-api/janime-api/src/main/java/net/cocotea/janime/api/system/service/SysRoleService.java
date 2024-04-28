package net.cocotea.janime.api.system.service;

import net.cocotea.janime.api.system.model.dto.SysRoleAddDTO;
import net.cocotea.janime.api.system.model.dto.SysRolePageDTO;
import net.cocotea.janime.api.system.model.dto.SysRoleUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysRoleMenuVO;
import net.cocotea.janime.api.system.model.vo.SysRoleVO;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.service.BaseService;

import java.math.BigInteger;
import java.util.List;

/**
 * 角色服务类
 * @date 2022-1-17 17:14:06
 * @author jwss
 */
public interface SysRoleService extends BaseService<ApiPage<SysRoleVO>, SysRolePageDTO, SysRoleAddDTO, SysRoleUpdateDTO> {
    /**
     * 给角色赋予权限
     * @param sysRoleMenuVOList 角色菜单列表
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    boolean grantPermissionsByRoleId(List<SysRoleMenuVO> sysRoleMenuVOList) throws BusinessException;

    /**
     * 通过用户ID获取角色
     * @param userId 用户id
     * @return 角色
     */
    List<SysRoleVO> loadByUserId(BigInteger userId);
}
