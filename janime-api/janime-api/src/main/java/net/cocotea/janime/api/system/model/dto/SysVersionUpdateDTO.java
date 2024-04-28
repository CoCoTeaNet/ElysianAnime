package net.cocotea.janime.api.system.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SysVersionUpdateDTO implements Serializable {

	private static final long serialVersionUID = 6284028262600112043L;

	@NotBlank(message = "id为空")
	private String id;
	private String updateNo;
	private String updateDesc;
	private String platformName;
	private String downloadUrl;

}
