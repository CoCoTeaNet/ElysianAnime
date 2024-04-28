package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.convert.Convert;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import com.sagframe.sagacity.sqltoy.plus.multi.MultiWrapper;
import com.sagframe.sagacity.sqltoy.plus.multi.model.LambdaColumn;
import net.cocotea.janime.api.system.model.dto.SysVersionAddDTO;
import net.cocotea.janime.api.system.model.dto.SysVersionPageDTO;
import net.cocotea.janime.api.system.model.dto.SysVersionUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysVersion;
import net.cocotea.janime.api.system.model.vo.SysVersionVO;
import net.cocotea.janime.api.system.service.SysVersionService;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import org.sagacity.sqltoy.model.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;

@Service
public class SysVersionServiceImpl implements SysVersionService {
    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Override
    public boolean add(SysVersionAddDTO param) throws BusinessException {
        SysVersion sysVersion = Convert.convert(SysVersion.class, param);
        Object save = sqlToyHelperDao.save(sysVersion);
        return save != null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatch(List<BigInteger> idList) throws BusinessException {
        for (BigInteger s : idList) {
            delete(s);
        }
        return !idList.isEmpty();
    }

    @Override
    public boolean update(SysVersionUpdateDTO param) throws BusinessException {
        SysVersion sysVersion = Convert.convert(SysVersion.class, param);
        Long count = sqlToyHelperDao.update(sysVersion);
        return count > 0;
    }

    @Override
    public ApiPage<SysVersionVO> listByPage(SysVersionPageDTO pageDTO) throws BusinessException {
        MultiWrapper multiWrapper = Wrappers.lambdaMultiWrapper(SysVersion.class)
                .select(
                        LambdaColumn.of(SysVersion::getId), LambdaColumn.of(SysVersion::getUpdateNo),
                        LambdaColumn.of(SysVersion::getPlatformName),
                        LambdaColumn.of(SysVersion::getUpdateDesc), LambdaColumn.of(SysVersion::getDownloadUrl),
                        LambdaColumn.of(SysVersion::getCreateTime), LambdaColumn.of(SysVersion::getUpdateTime)
                )
                .from(SysVersion.class)
                .where()
                .eq(SysVersion::getPlatformName, pageDTO.getSysVersion().getPlatformName())
                .eq(SysVersion::getUpdateNo, pageDTO.getSysVersion().getUpdateNo())
                .orderByDesc(SysVersion::getId);
        Page<SysVersionVO> page = sqlToyHelperDao.findPage(multiWrapper, new Page<>(pageDTO.getPageSize(), pageDTO.getPageNo()));
        return ApiPage.rest(page, SysVersionVO.class);
    }

    @Override
    public boolean delete(BigInteger id) throws BusinessException {
        return sqlToyHelperDao.delete(new SysVersion().setId(id)) > 0;
    }
}
