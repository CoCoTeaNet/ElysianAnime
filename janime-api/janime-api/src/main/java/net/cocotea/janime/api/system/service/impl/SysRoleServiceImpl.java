package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.janime.api.system.model.dto.SysRoleAddDTO;
import net.cocotea.janime.api.system.model.dto.SysRolePageDTO;
import net.cocotea.janime.api.system.model.dto.SysRoleUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysRole;
import net.cocotea.janime.api.system.model.po.SysRoleMenu;
import net.cocotea.janime.api.system.model.po.SysUserRole;
import net.cocotea.janime.api.system.model.vo.SysRoleMenuVO;
import net.cocotea.janime.api.system.model.vo.SysRoleVO;
import net.cocotea.janime.api.system.service.SysRoleService;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CoCoTea
 */
@Component
public class SysRoleServiceImpl implements SysRoleService {

    @Db("db1")
    private SqlToyLazyDao sqlToyLazyDao;

    @Override
    public boolean add(SysRoleAddDTO addDTO) throws BusinessException {
        SysRole sysRole = sqlToyLazyDao.convertType(addDTO, SysRole.class);
        HashMap<String, Object> map = MapUtil.newHashMap(1);
        map.put("roleKey", addDTO.getRoleKey());
        SysRole existSysRole = sqlToyLazyDao.loadBySql("sys_role_findList", map, SysRole.class);
        if (existSysRole != null) {
            throw new BusinessException("已存在该角色标识");
        }
        Object id = sqlToyLazyDao.save(sysRole);
        return id != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        List<SysRole> sysRoleList = new ArrayList<>();
        for (BigInteger id : idList) {
            SysRole article = new SysRole();
            article.setId(id);
            article.setIsDeleted(IsEnum.Y.getCode());
            sysRoleList.add(article);
        }
        Long count = sqlToyLazyDao.updateAll(sysRoleList);
        return count > 0;
    }

    @Override
    public boolean update(SysRoleUpdateDTO updateDTO) {
        SysRole sysRole = sqlToyLazyDao.convertType(updateDTO, SysRole.class);
        Long update = sqlToyLazyDao.update(sysRole);
        return update > 0;
    }

    @Tran
    @Override
    public boolean grantPermissionsByRoleId(List<SysRoleMenuVO> sysRoleMenuVOList) throws BusinessException {
        List<SysRoleMenu> sysRoleMenuList = sqlToyLazyDao.convertType(sysRoleMenuVOList, SysRoleMenu.class);
        if (sysRoleMenuList.isEmpty()) {
            throw new BusinessException("集合为空");
        }
        // 先删除所有权限再设置
        BigInteger roleId = sysRoleMenuList.get(0).getRoleId();
        EntityQuery entityQuery = EntityQuery.create().where("#[role_id = :roleId]").names("roleId").values(roleId);
        sqlToyLazyDao.deleteByQuery(SysRoleMenu.class, entityQuery);
        // 重新添加权限
        long saved = sqlToyLazyDao.saveOrUpdateAll(sysRoleMenuList);
        return saved > 0;
    }

    @Override
    public List<SysRoleVO> loadByUserId(BigInteger userId) {
        // 角色与用户关联
        HashMap<String, Object> userRoleMap = MapUtil.newHashMap(1);
        userRoleMap.put("userId", userId);
        List<BigInteger> roleIds = sqlToyLazyDao.findBySql("sys_user_role_findList", userRoleMap, SysUserRole.class).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        // 角色列表
        HashMap<String, Object> roleMap = MapUtil.newHashMap(1);
        roleMap.put("roleIds", roleIds);
        return sqlToyLazyDao.findBySql("sys_role_findList", roleMap, SysRoleVO.class);
    }

    @Tran
    @Override
    public boolean delete(BigInteger id) {
        // 删除角色
        sqlToyLazyDao.delete(new SysRole().setId(id));
        // 删除角色权限关联关系
        EntityQuery entityQuery = EntityQuery.create().where("#[role_id = :roleId]").names("roleId").values(id);
        long deleted = sqlToyLazyDao.delete(entityQuery);
        return deleted > 0;
    }

    @Override
    public ApiPage<SysRoleVO> listByPage(SysRolePageDTO pageDTO) {
        HashMap<String, Object> map = MapUtil.newHashMap(3);
        map.put("roleName", pageDTO.getSysRole().getRoleName());
        map.put("roleKey", pageDTO.getSysRole().getRoleKey());
        map.put("remark", pageDTO.getSysRole().getRemark());
        Page<SysRoleVO> page = sqlToyLazyDao.findPageBySql(ApiPage.create(pageDTO), "sys_role_findList", map, SysRoleVO.class);
        return ApiPage.rest(page);
    }
}
