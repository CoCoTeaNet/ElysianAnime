package net.cocotea.elysiananime.api.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.elysiananime.api.system.model.dto.*;
import net.cocotea.elysiananime.api.system.model.po.SysUser;
import net.cocotea.elysiananime.api.system.model.po.SysUserRole;
import net.cocotea.elysiananime.api.system.model.vo.SysLoginUserVO;
import net.cocotea.elysiananime.api.system.model.vo.SysMenuTreeVO;
import net.cocotea.elysiananime.api.system.model.vo.SysMenuVO;
import net.cocotea.elysiananime.api.system.model.vo.SysUserVO;
import net.cocotea.elysiananime.api.system.service.SysLogService;
import net.cocotea.elysiananime.api.system.service.SysMenuService;
import net.cocotea.elysiananime.api.system.service.SysRoleService;
import net.cocotea.elysiananime.api.system.service.SysUserService;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.service.RedisService;
import net.cocotea.elysiananime.common.util.TreeBuilder;
import net.cocotea.elysiananime.properties.DefaultProp;
import net.cocotea.elysiananime.util.LoginUtils;
import net.cocotea.elysiananime.util.SecurityUtils;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.data.annotation.Transaction;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.sagacity.sqltoy.utils.StringUtil;
import org.noear.solon.annotation.Component;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Slf4j
@Component
public class SysUserServiceImpl implements SysUserService {
    @Inject
    private DefaultProp defaultProp;

    @Inject
    private SysMenuService sysMenuService;

    @Inject
    private RedisService redisService;

    @Inject
    private SysLogService sysLogService;

    @Db("db1")
    private SqlToyLazyDao sqlToyLazyDao;

    @Db("db1")
    private LightDao lightDao;

    @Inject
    private SecurityUtils securityUtils;

    @Inject
    private SysRoleService sysRoleService;

    @Transaction
    @Override
    public boolean add(SysUserAddDTO addDTO) {
        SysUser sysUser = sqlToyLazyDao.convertType(addDTO, SysUser.class);
        if (StringUtil.isNotBlank(addDTO.getPassword())) {
            sysUser.setPassword(securityUtils.getPwd(addDTO.getPassword()));
        } else {
            sysUser.setPassword(defaultProp.getPassword());
        }
        Object userId = sqlToyLazyDao.save(sysUser);

        // 授予用户角色
        if (!(addDTO.getRoleIds().isEmpty())) {
            for (BigInteger roleId : addDTO.getRoleIds()) {
                SysUserRole sysUserRole = new SysUserRole().setUserId(LoginUtils.parse(userId)).setRoleId(roleId);
                sqlToyLazyDao.save(sysUserRole);
            }
        }

        return userId != null;
    }

    @Override
    public boolean delete(BigInteger id) {
        // 假删除，用户关联的数据不必操作
        SysUser sysUser = new SysUser().setId(id).setIsDeleted(IsEnum.Y.getCode());
        return sqlToyLazyDao.update(sysUser) > 0;
    }

    @Transaction
    @Override
    public boolean update(SysUserUpdateDTO updateDTO) {
        SysUser sysUser = Convert.convert(SysUser.class, updateDTO);
        if (!(updateDTO.getRoleIds() == null || updateDTO.getRoleIds().isEmpty())) {
            // 删除用户角色关联
            EntityQuery sysUserRoleQuery = EntityQuery.create().where("#[user_id = :userId]").names("userId").values(updateDTO.getId());
            sqlToyLazyDao.deleteByQuery(SysUserRole.class, sysUserRoleQuery);
            // 添加用户角色关联
            for (BigInteger roleId : updateDTO.getRoleIds()) {
                SysUserRole sysUserRole = new SysUserRole().setUserId(updateDTO.getId()).setRoleId(roleId);
                sqlToyLazyDao.save(sysUserRole);
            }
        }
        // 更新密码
        if (StringUtil.isNotBlank(updateDTO.getPassword())) {
            sysUser.setPassword(securityUtils.getPwd(updateDTO.getPassword()));
        }
        Long flag = sqlToyLazyDao.update(sysUser);
        return flag > 0;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        if (idList != null) {
            idList.forEach(this::delete);
        }
        return idList != null && !idList.isEmpty();
    }

    @Override
    public ApiPage<SysUserVO> listByPage(SysUserPageDTO pageDTO) {
        Map<String, Object> params = BeanUtil.beanToMap(pageDTO.getSysUser());

        Page<SysUserVO> page = sqlToyLazyDao.findPageBySql(ApiPage.create(pageDTO), "sys_user_findList", params, SysUserVO.class);
        page.getRows().forEach(row -> row.setRoleList(sysRoleService.loadByUserId(row.getId())));
        return ApiPage.rest(page);
    }

    @Override
    public String login(SysLoginDTO loginDTO, Context context) throws BusinessException {
        SysUser sysUser = getOneOfCache(loginDTO.getUsername());
        if (sysUser == null) {
            throw new BusinessException("登录失败，用户不存在");
        }
        // 校验密码
        String pwd = securityUtils.getPwd(loginDTO.getPassword());
        if (ObjUtil.notEqual(sysUser.getPassword(), pwd)) {
            throw new BusinessException("登录失败，用户名或密码错误");
        }
        // 记住我模式
        if (loginDTO.getRememberMe()) {
            StpUtil.login(sysUser.getId(), new SaLoginParameter().setTimeout(3600 * 24 * 365));
        } else {
            StpUtil.login(sysUser.getId());
        }
        // 更新用户登录时间和ip
        SysUser loginSysUser = new SysUser();
        loginSysUser.setId(sysUser.getId());
        loginSysUser.setLastLoginIp(context.realIp());
        loginSysUser.setLastLoginTime(LocalDateTime.now());
        sqlToyLazyDao.update(loginSysUser);
        return StpUtil.getTokenValue();
    }

    @Override
    public SysUserVO getDetail() {
        BigInteger loginId = LoginUtils.loginId();
        // 用户信息
        SysUser sysUser = sqlToyLazyDao.loadBySql("sys_user_getOne", new SysUser().setId(loginId));
        SysUserVO sysUserVO = Convert.convert(SysUserVO.class, sysUser);
        return sysUserVO.setRoleList(sysRoleService.loadByUserId(loginId));
    }

    @Override
    public SysLoginUserVO loginUser() {
        BigInteger loginId = LoginUtils.loginIdEx();
        SysUser sysUser = sqlToyLazyDao.loadBySql("sys_user_getOne", new SysUser().setId(loginId));
        SysLoginUserVO sysLoginUser = new SysLoginUserVO();
        // 用户菜单
        List<SysMenuVO> menuList = sysMenuService.listByUserId(IsEnum.Y.getCode());
        menuList = new TreeBuilder<SysMenuVO>().get(menuList);
        sysLoginUser.setMenuList(Convert.toList(SysMenuTreeVO.class, menuList));
        // 用户基本信息
        sysLoginUser.setOrigin(defaultProp.getWebUrl())
                .setUsername(sysUser.getUsername())
                .setNickname(sysUser.getNickname())
                .setAvatar(sysUser.getAvatar())
                .setId(sysUser.getId())
                .setLoginStatus(true)
                .setToken(StpUtil.getTokenValue());
        return sysLoginUser;
    }

    @Override
    public boolean updateByUser(SysLoginUserUpdateDTO param) {
        SysUser sysUser = Convert.convert(SysUser.class, param);
        sysUser.setId(LoginUtils.loginId());
        return sqlToyLazyDao.update(sysUser) > 0;
    }

    @Override
    public boolean doModifyPassword(String oldPassword, String newPassword) throws BusinessException {
        if (StringUtil.isBlank(oldPassword)) {
            throw new BusinessException("旧密码为空");
        }
        if (StringUtil.isBlank(newPassword)) {
            throw new BusinessException("新密码为空");
        }
        BigInteger loginId = LoginUtils.loginId();
        SysUser sysUser = sqlToyLazyDao.loadBySql("sys_user_getOne", new SysUser().setId(loginId));
        String pwdOld = securityUtils.getPwd(oldPassword);
        if (!sysUser.getPassword().equals(pwdOld)) {
            throw new BusinessException("旧密码不正确");
        }
        String pwdNew = securityUtils.getPwd(newPassword);
        sysUser.setPassword(pwdNew);
        return sqlToyLazyDao.update(sysUser) > 0;
    }

    @Override
    public Map<BigInteger, SysUser> getMap(List<BigInteger> ids) {
        HashMap<String, Object> map = MapUtil.newHashMap(1);
        map.put("ids", ids);
        List<SysUser> list = sqlToyLazyDao.findBySql("sys_user_findList", map, SysUser.class);
        return list.stream().collect(Collectors.toMap(SysUser::getId, i -> i));
    }

    @Override
    public void doModifyAvatar(String avatarName) {
        BigInteger loginId = LoginUtils.loginId();
        SysUser sysUser = new SysUser().setId(loginId).setAvatar(avatarName);
        sqlToyLazyDao.update(sysUser);
    }

    @Override
    public SysUser getOne(String username) throws BusinessException {
        if (StrUtil.isBlank(username)) {
            throw new BusinessException("用户名不能为空");
        }
        return lightDao.findOne("sys_user_getOne", new SysUser().setUsername(username), SysUser.class);
    }

    @Override
    public SysUser getOneOfCache(String username) throws BusinessException {
        SysUser user;
        String key = String.format(RedisKeyConst.CACHE_USERINFO, username);
        String existUser = redisService.get(key);
        if (existUser == null) {
            user = getOne(username);
            redisService.save(key, JSON.toJSONString(user));
        } else {
            user = JSON.parseObject(existUser, SysUser.class);
        }
        return user;
    }
}
