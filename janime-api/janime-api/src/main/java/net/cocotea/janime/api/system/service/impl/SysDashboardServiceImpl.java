package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.system.HostInfo;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import net.cocotea.janime.api.system.model.po.SysMenu;
import net.cocotea.janime.api.system.model.po.SysRole;
import net.cocotea.janime.api.system.model.po.SysUser;
import net.cocotea.janime.api.system.model.vo.SysOverviewVO;
import net.cocotea.janime.api.system.model.vo.SystemInfoVO;
import net.cocotea.janime.api.system.service.SysDashboardService;
import net.cocotea.janime.common.constant.GlobalConst;
import net.cocotea.janime.common.constant.RedisKeyConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.enums.MenuTypeEnum;
import net.cocotea.janime.common.service.RedisService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.solon.annotation.Db;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CoCoTea
 */
@Component
public class SysDashboardServiceImpl implements SysDashboardService {
    
    @Db("db1")
    private SqlToyLazyDao sqlToyLazyDao;
    
    @Inject
    private RedisService redisService;

    @Override
    public List<SysOverviewVO> getCount() {
        List<SysOverviewVO> sysOverviewList = new ArrayList<>(4);

        HashMap<String, Object> paramsMap = MapUtil.newHashMap();
        paramsMap.put("isDeleted", IsEnum.N.getCode());
        long countUser = sqlToyLazyDao.getCount("select count(1) from sys_user where is_deleted=:isDeleted", paramsMap);
        sysOverviewList.add(new SysOverviewVO().setTitle("用户数量").setCount(countUser));

        paramsMap = MapUtil.newHashMap();
        paramsMap.put("isDeleted", IsEnum.N.getCode());
        paramsMap.put("isMenu", IsEnum.Y.getCode());
        paramsMap.put("menuType", MenuTypeEnum.MENU.getCode());
        long countMenu = sqlToyLazyDao.getCount("select count(1) from sys_menu where is_deleted=:isDeleted and is_menu = :isMenu and menu_type = :menuType", paramsMap);
        sysOverviewList.add(new SysOverviewVO().setTitle("菜单数量").setCount(countMenu));

        paramsMap = MapUtil.newHashMap();
        paramsMap.put("isDeleted", IsEnum.N.getCode());
        long countRole = sqlToyLazyDao.getCount("select count(1) from sys_role where is_deleted=:isDeleted", paramsMap);
        sysOverviewList.add(new SysOverviewVO().setTitle("角色数量").setCount(countRole));

        long countOnline = redisService.keys(
                String.format(RedisKeyConst.ONLINE_USER, "*")
        ).size();
        sysOverviewList.add(new SysOverviewVO().setTitle("在线用户").setCount(countOnline));
        return sysOverviewList;
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
