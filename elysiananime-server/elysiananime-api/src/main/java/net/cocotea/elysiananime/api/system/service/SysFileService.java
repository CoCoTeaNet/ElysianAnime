package net.cocotea.elysiananime.api.system.service;

import net.cocotea.elysiananime.api.system.model.dto.SysFileAddDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysFilePageDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysFileUpdateDTO;
import net.cocotea.elysiananime.api.system.model.vo.SysFileVO;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.service.BaseService;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统文件表
 *
 * @author CoCoTea 572315466@qq.com
 * @version 2.0.0
 */
public interface SysFileService extends BaseService<ApiPage<SysFileVO>, SysFilePageDTO, SysFileAddDTO, SysFileUpdateDTO> {
    /**
     * 数据为删除状态的数据
     *
     * @param param 系统文件分页参数
     * @return 分页对象
     */
    ApiPage<SysFileVO> recycleBinPage(SysFilePageDTO param);

    /**
     * 删除回收站的文件
     *
     * @param param 列表id
     * @return 成功返回TRUE
     */
    boolean recycleBinDeleteBatch(List<BigInteger> param);

    /**
     * 恢复已删除的文件
     *
     * @param param 主键ID集合
     * @return 成功返回TRUE
     */
    boolean recoveryBatch(List<BigInteger> param);

    // /**
    //  * 下载文件
    //  *
    //  * @param fileId 文件ID
    //  * @param response {@link HttpServletResponse}
    //  */
    // void download(BigInteger fileId, HttpServletResponse response) throws BusinessException, IOException;

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return {@link SysFileVO}
     */
    SysFileVO getFile(BigInteger fileId) throws BusinessException;

    /**
     * 获取某个用户上传的文件信息
     *
     * @param fileId 文件ID
     * @return {@link SysFileVO}
     */
    SysFileVO getUserFile(BigInteger fileId) throws BusinessException;
}