package net.cocotea.elysiananime.api.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import net.cocotea.elysiananime.api.system.model.dto.SysFileAddDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysFilePageDTO;
import net.cocotea.elysiananime.api.system.model.dto.SysFileUpdateDTO;
import net.cocotea.elysiananime.api.system.model.po.SysFile;
import net.cocotea.elysiananime.api.system.model.vo.SysFileVO;
import net.cocotea.elysiananime.api.system.service.SysFileService;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.model.ApiPage;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.properties.FileProp;
import net.cocotea.elysiananime.util.LoginUtils;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统文件表
 *
 * @author CoCoTea 572315466@qq.com
 * @version 2.0.0
 */
@Component
public class SysFileServiceImpl implements SysFileService {

    @Db("db1")
    private SqlToyLazyDao sqlToyLazyDao;

    @Db("db1")
    private LightDao lightDao;

    @Inject
    private FileProp fileProp;

    @Override
    public boolean add(SysFileAddDTO fileAddDTO) {
        SysFile sysFile = Convert.convert(SysFile.class, fileAddDTO);
        Object save = sqlToyLazyDao.save(sysFile);
        return save != null;
    }

    @Tran
    @Override
    public boolean deleteBatch(List<BigInteger> param) {
        List<SysFile> sysFileList = new ArrayList<>(param.size());
        for (BigInteger id : param) {
            sysFileList.add(new SysFile().setId(id).setIsDeleted(IsEnum.Y.getCode()));
        }
        return sqlToyLazyDao.updateAll(sysFileList) > 0;
    }

    @Override
    public boolean update(SysFileUpdateDTO fileUpdateDTO) {
        SysFile sysFile = Convert.convert(SysFile.class, fileUpdateDTO);
        Long count = sqlToyLazyDao.update(sysFile);
        return count > 0;
    }

    @Override
    public ApiPage<SysFileVO> listByPage(SysFilePageDTO pageDTO) {
        Map<String, Object> sysFileMap = BeanUtil.beanToMap(pageDTO.getSysFile());
        sysFileMap.put("userId", LoginUtils.loginId());

        Page<SysFileVO> page = lightDao.findPage(ApiPage.create(pageDTO), "sys_file_JOIN_findList", sysFileMap, SysFileVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        return sqlToyLazyDao.update(new SysFile().setId(id).setIsDeleted(IsEnum.Y.getCode())) > 0;
    }

    @Override
    public ApiPage<SysFileVO> recycleBinPage(SysFilePageDTO pageDTO) {
        return listByPage(pageDTO.setIsDeleted(IsEnum.Y.getCode()));
    }

    @Tran
    @Override
    public boolean recycleBinDeleteBatch(List<BigInteger> param) {
        List<SysFile> sysFileList = new ArrayList<>(param.size());
        for (BigInteger id : param) {
            SysFile sysFile = sqlToyLazyDao.load(new SysFile().setId(id));
            if (sysFile != null) {
                FileUtil.del(fileProp.getDefaultSavePath() + sysFile.getRealPath());
            }
            sysFileList.add(new SysFile().setId(id));
        }
        return sqlToyLazyDao.deleteAll(sysFileList) > 0;
    }

    @Override
    public boolean recoveryBatch(List<BigInteger> param) {
        List<SysFile> sysFileList = new ArrayList<>(param.size());
        for (BigInteger id : param) {
            sysFileList.add(new SysFile().setId(id).setIsDeleted(IsEnum.N.getCode()));
        }
        return sqlToyLazyDao.updateAll(sysFileList) > 0;
    }

    @Override
    public SysFileVO getFile(BigInteger fileId) throws BusinessException {
        SysFile sysFile = sqlToyLazyDao.load(new SysFile().setId(fileId));
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        return Convert.convert(SysFileVO.class, sysFile);
    }

    @Override
    public SysFileVO getUserFile(BigInteger fileId) throws BusinessException {
        SysFileVO sysFile = getFile(fileId);
        boolean isShare = sysFile.getIsShare() == IsEnum.Y.getCode().intValue();
        if (!isShare) {
            BigInteger loginId = LoginUtils.loginId();
            if (!String.valueOf(loginId).equals(sysFile.getCreateBy())) {
                throw new BusinessException("无权限查看");
            }
        }
        return sysFile;
    }

}