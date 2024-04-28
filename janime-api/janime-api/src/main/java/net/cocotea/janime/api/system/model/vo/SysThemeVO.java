package net.cocotea.janime.api.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
* 系统主题表
*
* @author CoCoTea 572315466@qq.com
* @since 1.2.4 2023-02-25
*/
@Data
@Accessors(chain = true)
public class SysThemeVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *系统主题ID
	 */
	private String ID;

	/**
	 *用户ID
	 */
	private String userId;

	/**
	 *布局模式：0默认
	 */
	private Integer layoutMode;

	/**
	 *主题颜色
	 */
	private String primaryColor;

	/**
	 *颜色2
	 */
	private String color2;

	/**
	 *颜色3
	 */
	private String color3;

	/**
	 *颜色4
	 */
	private String color4;

	/**
	 *是否暗黑模式：0否 1是
	 */
	private Integer isDark;

}