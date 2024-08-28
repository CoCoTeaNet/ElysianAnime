package net.cocotea.elysiananime.api.system.model.dto;


import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;
import java.io.Serializable;

/**
* 系统主题表
*
* @author CoCoTea 572315466@qq.com
* @since 1.2.4 2023-02-25
*/
@Data
@Accessors(chain = true)
public class SysThemeUpdateDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 *系统主题ID
	 */
	@NotBlank(message = "系统主题ID不能为空")
	private String id;

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