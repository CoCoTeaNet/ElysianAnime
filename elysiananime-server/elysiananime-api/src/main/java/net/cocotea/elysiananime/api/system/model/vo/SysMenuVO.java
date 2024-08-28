package net.cocotea.elysiananime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysMenuVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1020925827373568354L;

	/**
	 * 菜单ID
	 */
	private BigInteger id;

	/**
	 * 菜单名称
	 */
	private String menuName;

	/**
	 * 权限编号
	 */
	private String permissionCode;

	/**
	 * 路由路径
	 */
	private String routerPath;

	/**
	 * 父菜单ID
	 */
	private BigInteger parentId;

	/**
	 * 菜单类型
	 */
	private Integer menuType;

	/**
	 * 菜单状态
	 */
	private Integer menuStatus;

	/**
	 * 组件路径
	 */
	private String componentPath;

	/**
	 * 是否拓展链接
	 */
	private Integer isExternalLink;

	/**
	 * 图标路径
	 */
	private String iconPath;

	/**
	 * 序号
	 */
	private Integer sort;

	/**
	 * 创建人
	 */
	private BigInteger createBy;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 更新人
	 */
	private BigInteger updateBy;

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;

	/**
	 * 是否菜单
	 */
	private Integer isMenu;

	/**
	 * 子项
	 */
	private List<SysMenuVO> children;

}
