package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import com.sagframe.sagacity.sqltoy.plus.multi.MultiWrapper;
import com.sagframe.sagacity.sqltoy.plus.multi.model.LambdaColumn;
import net.cocotea.janime.api.system.model.dto.SysFileAddDTO;
import net.cocotea.janime.api.system.model.dto.SysFilePageDTO;
import net.cocotea.janime.api.system.model.dto.SysFileUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysFile;
import net.cocotea.janime.api.system.model.po.SysUser;
import net.cocotea.janime.api.system.model.vo.SysFileVO;
import net.cocotea.janime.api.system.model.vo.SysLogVO;
import net.cocotea.janime.api.system.service.SysFileService;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.properties.FileProp;
import net.cocotea.janime.util.LoginUtils;
import org.sagacity.sqltoy.model.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统文件表
 *
 * @author CoCoTea 572315466@qq.com
 * @version 2.0.0
 */
@Service
public class SysFileServiceImpl implements SysFileService {

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Resource
    private FileProp fileProp;

    @Override
    public boolean add(SysFileAddDTO fileAddDTO) {
        SysFile sysFile = Convert.convert(SysFile.class, fileAddDTO);
        Object save = sqlToyHelperDao.save(sysFile);
        return save != null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatch(List<BigInteger> param) {
        List<SysFile> sysFileList = new ArrayList<>(param.size());
        for (BigInteger id : param) {
            sysFileList.add(new SysFile().setId(id).setIsDeleted(IsEnum.Y.getCode()));
        }
        return sqlToyHelperDao.updateAll(sysFileList) > 0;
    }

    @Override
    public boolean update(SysFileUpdateDTO fileUpdateDTO) {
        SysFile sysFile = Convert.convert(SysFile.class, fileUpdateDTO);
        Long count = sqlToyHelperDao.update(sysFile);
        return count > 0;
    }

    @Override
    public ApiPage<SysFileVO> listByPage(SysFilePageDTO pageDTO) {
        MultiWrapper multiWrapper = Wrappers.lambdaMultiWrapper(SysFile.class)
                .select(
                        LambdaColumn.of(SysFile::getId), LambdaColumn.of(SysFile::getFileName),
                        LambdaColumn.of(SysFile::getFileSuffix), LambdaColumn.of(SysFile::getFileSize),
                        LambdaColumn.of(SysFile::getCreateTime), LambdaColumn.of(SysFile::getUpdateTime),
                        LambdaColumn.of(SysFile::getIsShare),
                        LambdaColumn.of(SysUser::getUsername), LambdaColumn.of(SysUser::getNickname)
                )
                .from(SysFile.class)
                .leftJoin(SysUser.class).on().eq(SysFile::getCreateBy, SysUser::getId)
                .where()
                .eq(SysFile::getIsDeleted, pageDTO.getIsDeleted())
                .eq(SysFile::getFileSuffix, pageDTO.getSysFile().getFileSuffix())
                .eq(SysFile::getCreateBy, LoginUtils.loginId())
                .between(SysFile::getCreateTime, pageDTO.getSysFile().getBeginTime(), pageDTO.getSysFile().getEndTime())
                .orderByDesc(SysFile::getId);
        Page<SysLogVO> page = sqlToyHelperDao.findPage(multiWrapper, new Page<>(pageDTO.getPageSize(), pageDTO.getPageNo()));
        return ApiPage.rest(page, SysFileVO.class);
    }

    @Override
    public boolean delete(BigInteger id) {
        return sqlToyHelperDao.update(new SysFile().setId(id).setIsDeleted(IsEnum.Y.getCode())) > 0;
    }

    @Override
    public ApiPage<SysFileVO> recycleBinPage(SysFilePageDTO pageDTO) {
        return listByPage(pageDTO.setIsDeleted(IsEnum.Y.getCode()));
    }

    @Transactional(rollbackFor = BusinessException.class)
    @Override
    public boolean recycleBinDeleteBatch(List<BigInteger> param) {
        List<SysFile> sysFileList = new ArrayList<>(param.size());
        for (BigInteger id : param) {
            SysFile sysFile = sqlToyHelperDao.load(new SysFile().setId(id));
            if (sysFile != null) {
                FileUtil.del(fileProp.getDefaultSavePath() + sysFile.getRealPath());
            }
            sysFileList.add(new SysFile().setId(id));
        }
        return sqlToyHelperDao.deleteAll(sysFileList) > 0;
    }

    @Override
    public boolean recoveryBatch(List<BigInteger> param) {
        List<SysFile> sysFileList = new ArrayList<>(param.size());
        for (BigInteger id : param) {
            sysFileList.add(new SysFile().setId(id).setIsDeleted(IsEnum.N.getCode()));
        }
        return sqlToyHelperDao.updateAll(sysFileList) > 0;
    }

    @Override
    public void download(BigInteger fileId, HttpServletResponse response) throws BusinessException, IOException {
        SysFileVO sysFile = getUserFile(fileId);
        String fullPath = fileProp.getDefaultSavePath() + sysFile.getRealPath();
        File file = FileUtil.file(fullPath);
        if (file.exists()) {
            FileUtil.writeToStream(file, response.getOutputStream());
        } else {
            throw new BusinessException("文件不存在");
        }
    }

    @Override
    public SysFileVO getFile(BigInteger fileId) throws BusinessException {
        SysFile sysFile = sqlToyHelperDao.load(new SysFile().setId(fileId));
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