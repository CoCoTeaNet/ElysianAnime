package net.cocotea.janime.api.system.model.dto;

import cn.hutool.core.convert.Convert;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.api.system.model.po.SysUser;
import net.cocotea.janime.api.system.model.vo.SysUserVO;
import org.sagacity.sqltoy.model.Page;

import java.io.Serializable;

/**
 * @author jwss
 * @project sss-rbac-admin
 * @version 1.0.0
 * @description sys_menu,系统菜单表  
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysUserPageDTO extends Page<SysUserVO> implements Serializable{

	private static final long serialVersionUID = 5565588370362046172L;

	private SysUserVO sysUser;

	public SysUser getPO() {
		return Convert.convert(SysUser.class, sysUser);
	}
}
