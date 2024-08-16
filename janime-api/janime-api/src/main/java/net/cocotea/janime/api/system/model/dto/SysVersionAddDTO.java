package net.cocotea.janime.api.system.model.dto;


import lombok.Data;
import lombok.experimental.Accessors;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SysVersionAddDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1746246243402738341L;

	/**
	 * 版本号
	 */
	@NotBlank(message = "版本号不能为空")
	private String updateNo;

	/**
	 * 更新描述
	 */
	private String updateDesc;

	/**
	 * 平台名称
	 */
	private String platformName;

	/**
	 * 下载地址
	 */
	private String downloadUrl;

}
