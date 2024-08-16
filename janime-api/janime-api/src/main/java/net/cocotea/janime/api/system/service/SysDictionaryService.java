package net.cocotea.janime.api.system.service;

import net.cocotea.janime.api.system.model.dto.SysDictionaryAddDTO;
import net.cocotea.janime.api.system.model.dto.SysDictionaryPageDTO;
import net.cocotea.janime.api.system.model.dto.SysDictionaryTreeDTO;
import net.cocotea.janime.api.system.model.dto.SysDictionaryUpdateDTO;
import net.cocotea.janime.api.system.model.vo.SysDictionaryVO;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.service.BaseService;

import java.util.List;

/**
 * 字典 接口服务类
 * @author CoCoTea
 * @version 2.0.0
 */
public interface SysDictionaryService extends BaseService<ApiPage<SysDictionaryVO>, SysDictionaryPageDTO, SysDictionaryAddDTO, SysDictionaryUpdateDTO> {

    /**
     * 获取树形结构
     * @param param 分页参数
     * @return 分页对象
     */
    List<SysDictionaryVO> listByTree(SysDictionaryTreeDTO param);
}
