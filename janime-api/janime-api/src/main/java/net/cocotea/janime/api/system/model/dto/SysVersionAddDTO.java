package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SysVersionAddDTO implements Serializable {
	private static final long serialVersionUID = 1746246243402738341L;

	@NotBlank(message = "版本号不能为空")
	private String updateNo;
	private String updateDesc;
	private String platformName;
	private String downloadUrl;

}
