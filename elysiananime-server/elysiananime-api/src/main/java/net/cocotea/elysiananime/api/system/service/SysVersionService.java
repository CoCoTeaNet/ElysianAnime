package net.cocotea.elysiananime.api.system.service;

import net.cocotea.elysiananime.api.system.model.dto.SysVersionAddDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysVersionPageDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysVersionUpdateDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysVersionVO;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.service.BaseService;

public interface SysVersionService extends BaseService<ApiPage<SysVersionVO>, SysVersionPageDTO, SysVersionAddDTO, SysVersionUpdateDTO> {
}
