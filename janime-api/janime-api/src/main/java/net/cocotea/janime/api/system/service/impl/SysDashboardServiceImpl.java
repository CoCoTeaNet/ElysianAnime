package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.system.HostInfo;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.system.model.po.SysMenu;
import net.cocotea.janime.api.system.model.po.SysRole;
import net.cocotea.janime.api.system.model.po.SysUser;
import net.cocotea.janime.api.system.model.vo.SystemInfoVO;
import net.cocotea.janime.api.system.service.SysDashboardService;
import net.cocotea.janime.common.constant.CharConst;
import net.cocotea.janime.common.constant.GlobalConst;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.enums.MenuTypeEnum;
import net.cocotea.janime.common.service.RedisService;
import org.springframework.stereotype.Service;
import oshi.hardware.GlobalMemory;

import javax.annotation.Resource;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CoCoTea
 */
@Service
public class SysDashboardServiceImpl implements SysDashboardService {
    @Resource
    private SqlToyHelperDao sqlToyHelperDao;
    @Resource
    private RedisService redisService;

    @Override
    public List<Map<String, Object>> getCount() {
        Map<String, Object> hashMap;

        List<Map<String, Object>> mapList = new ArrayList<>(4);
        LambdaQueryWrapper<SysUser> sysUserWrapper = new LambdaQueryWrapper<>(SysUser.class)
                .select().eq(SysUser::getIsDeleted, IsEnum.N.getCode());
        Long countUser = sqlToyHelperDao.count(sysUserWrapper);
        hashMap = new HashMap<>(2);
        hashMap.put("title", "用户数量");
        hashMap.put("count", countUser);
        mapList.add(hashMap);

        LambdaQueryWrapper<SysMenu> sysMenuWrapper = new LambdaQueryWrapper<>(SysMenu.class)
                .select()
                .eq(SysMenu::getIsDeleted, IsEnum.N.getCode())
                .eq(SysMenu::getIsMenu, IsEnum.Y.getCode())
                .eq(SysMenu::getMenuType, MenuTypeEnum.MENU.getCode());
        Long countMenu = sqlToyHelperDao.count(sysMenuWrapper);
        hashMap = new HashMap<>(2);
        hashMap.put("title", "菜单数量");
        hashMap.put("count", countMenu);
        mapList.add(hashMap);

        LambdaQueryWrapper<SysRole> sysRoleWrapper = new LambdaQueryWrapper<>(SysRole.class)
                .select().eq(SysRole::getIsDeleted, IsEnum.N.getCode());
        long countRole = sqlToyHelperDao.count(sysRoleWrapper);
        hashMap = new HashMap<>(2);
        hashMap.put("title", "角色数量");
        hashMap.put("count", countRole);
        mapList.add(hashMap);

        Long countOnline = (long) redisService.keys(
                String.format(RedisKeyConst.ONLINE_USER, CharConst.ASTERISK)
        ).size();
        hashMap = new HashMap<>(2);
        hashMap.put("title", "在线用户");
        hashMap.put("count", countOnline);
        mapList.add(hashMap);
        return mapList;
    }

    @Override
    public SystemInfoVO getSystemInfo() {
        SystemInfoVO systemInfoVO = new SystemInfoVO();
        // 服务器信息
        OsInfo osInfo = SystemUtil.getOsInfo();
        HostInfo hostInfo = SystemUtil.getHostInfo();
        systemInfoVO.setOs(osInfo.getName());
        systemInfoVO.setServerName(hostInfo.getName());
        systemInfoVO.setServerIp(hostInfo.getAddress());
        systemInfoVO.setServerArchitecture(osInfo.getArch());
        // java信息
        systemInfoVO.setJavaName(SystemUtil.getJvmInfo().getName());
        systemInfoVO.setJavaVersion(SystemUtil.getJavaInfo().getVersion());
        systemInfoVO.setJavaPath(SystemUtil.getJavaRuntimeInfo().getHomeDir());
        // 服务运行信息
        systemInfoVO.setProjectPath(System.getProperty("user.dir"));
        Duration between = LocalDateTimeUtil.between(
                LocalDateTimeUtil.ofUTC(GlobalConst.START_TIME),
                LocalDateTimeUtil.ofUTC(System.currentTimeMillis())
        );
        systemInfoVO.setRunningTime(between.getSeconds());
        // CPU信息
        CpuInfo cpuInfo = OshiUtil.getCpuInfo();
        systemInfoVO.setCpuSystemUsed(cpuInfo.getSys());
        systemInfoVO.setCpuUserUsed(cpuInfo.getUsed());
        systemInfoVO.setCpuCount(cpuInfo.getCpuNum());
        systemInfoVO.setCpuFree(cpuInfo.getFree());
        // 内存信息
        GlobalMemory memory = OshiUtil.getMemory();
        systemInfoVO.setMemoryAvailableSize(memory.getAvailable());
        systemInfoVO.setMemoryTotalSize(memory.getTotal());
        // 磁盘信息
        File file = new File(SystemUtil.get(SystemUtil.FILE_SEPARATOR));
        systemInfoVO.setDiskTotalSize(file.getTotalSpace());
        systemInfoVO.setDiskFreeSize(file.getFreeSpace());
        systemInfoVO.setDiskPath(SystemUtil.get(SystemUtil.FILE_SEPARATOR));
        systemInfoVO.setDiskSeparator(SystemUtil.get(SystemUtil.FILE_SEPARATOR));
        return systemInfoVO;
    }
}
