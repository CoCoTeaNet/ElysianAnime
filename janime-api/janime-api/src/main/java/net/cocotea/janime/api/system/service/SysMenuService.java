package net.cocotea.janime.api.system.service;

import net.cocotea.janime.api.system.model.dto.SysMenuAddDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuPageDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuTreeDTO;
import net.cocotea.janime.api.system.model.dto.SysMenuUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysMenuVO;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.service.BaseService;

import java.math.BigInteger;
import java.util.List;

/**
 * @author CoCoTea
 * @date 2022-1-16 15:47:03
 */
public interface SysMenuService extends BaseService<ApiPage<SysMenuVO>, SysMenuPageDTO, SysMenuAddDTO, SysMenuUpdateDTO> {
    /**
     * 分页查询菜单
     *
     * @param menuTreeDTO 分页参数
     * @return 分页结果集
     */
    List<SysMenuVO> listByTree(SysMenuTreeDTO menuTreeDTO);

    /**
     * 获取用户的所有菜单
     *
     * @param isMenu 是否菜单
     * @return 用户菜单集合
     */
    List<SysMenuVO> listByUserId(Integer isMenu);

    /**
     * 获取角色请所有菜单
     *
     * @param roleId 角色主键id
     * @return 菜单列表
     */
    List<SysMenuVO> listByRoleId(String roleId);

    /**
     * 缓存用户权限
     *
     * @param userId 用户主键id
     * @return 用户权限
     */
    List<SysMenuVO> cachePermission(BigInteger userId);

    /**
     * 获取用户缓存权限
     *
     * @param userId 用户主键id
     * @return 用户权限
     */
    List<SysMenuVO> getCachePermission(BigInteger userId);

    /**
     * 通关角色获取菜单下拉选项
     *
     * @param pageDTO {@linkplain SysMenuTreeDTO}
     * @return {@link SysMenuVO}
     */
    List<SysMenuVO> listByTreeAsRoleSelection(SysMenuTreeDTO pageDTO);
}
