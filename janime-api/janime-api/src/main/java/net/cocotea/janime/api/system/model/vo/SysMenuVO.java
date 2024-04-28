package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysMenuVO {

	private BigInteger id;
	private String menuName;
	private String permissionCode;
	private String routerPath;
	private BigInteger parentId;
	private Integer menuType;
	private Integer menuStatus;
	private String componentPath;
	private Integer isExternalLink;
	private String iconPath;
	private Integer sort;
	private BigInteger createBy;
	private LocalDateTime createTime;
	private BigInteger updateBy;
	private LocalDateTime updateTime;
	private Integer isMenu;

	private List<SysMenuVO> children;

}
