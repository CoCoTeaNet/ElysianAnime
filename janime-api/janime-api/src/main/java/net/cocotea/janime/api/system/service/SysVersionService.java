package net.cocotea.janime.api.system.service;

import net.cocotea.janime.api.system.model.dto.SysVersionAddDTO;
import net.cocotea.janime.api.system.model.dto.SysVersionPageDTO;
import net.cocotea.janime.api.system.model.dto.SysVersionUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysVersionVO;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.service.BaseService;

public interface SysVersionService extends BaseService<ApiPage<SysVersionVO>, SysVersionPageDTO, SysVersionAddDTO, SysVersionUpdateDTO> {
}
