package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.janime.common.model.ApiPageDTO;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysVersionPageDTO extends ApiPageDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 2569886533383934522L;

	@NotNull(message = "查询参数为空")
	private Query sysVersion;

	@Data
	public static class Query {

		/**
		 * 系统平台名称
		 */
		private String platformName;

		/**
		 * 更新版本号
		 */
		private String updateNo;

	}

}
