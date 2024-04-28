package net.cocotea.janime.api.system.service.impl;

import cn.hutool.core.convert.Convert;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.system.model.dto.SysDictionaryAddDTO;
import net.cocotea.janime.api.system.model.dto.SysDictionaryPageDTO;
import net.cocotea.janime.api.system.model.dto.SysDictionaryUpdateDTO;
import net.cocotea.janime.api.system.model.po.SysDictionary;
import net.cocotea.janime.api.system.model.po.SysUser;
import net.cocotea.janime.api.system.model.vo.SysDictionaryVO;
import net.cocotea.janime.api.system.service.SysDictionaryService;
import net.cocotea.janime.common.constant.CharConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.util.TreeBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDictionaryServiceImpl implements SysDictionaryService {
    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Override
    public boolean add(SysDictionaryAddDTO addDTO) {
        SysDictionary sysDictionary = sqlToyHelperDao.convertType(addDTO, SysDictionary.class);
        if (sysDictionary.getParentId() == null) {
            sysDictionary.setParentId(BigInteger.ZERO);
        }
        if (sysDictionary.getSort() == null) {
            sysDictionary.setSort(0);
        }
        Object o = sqlToyHelperDao.save(sysDictionary);
        return o != null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(SysDictionaryUpdateDTO param) {
        SysDictionary sysDictionary = sqlToyHelperDao.convertType(param, SysDictionary.class);
        Long update = sqlToyHelperDao.update(sysDictionary);
        return update != null;
    }

    @Override
    public ApiPage<SysDictionaryVO> listByPage(SysDictionaryPageDTO dto) {
        return null;
    }

    @Override
    public List<SysDictionaryVO> listByTree(SysDictionaryPageDTO dto) {
        return new TreeBuilder<SysDictionaryVO>().get(findList(dto.getSysDictionary()));
    }

    private List<SysDictionaryVO> findList(SysDictionaryVO sysDictionaryVO) {
        LambdaQueryWrapper<SysDictionary> wrapper = new LambdaQueryWrapper<>(SysDictionary.class)
                .select()
                .eq(SysDictionary::getIsDeleted, IsEnum.N.getCode())
                .eq(SysDictionary::getEnableStatus, sysDictionaryVO.getEnableStatus())
                .like(SysDictionary::getDictionaryName, sysDictionaryVO.getDictionaryName())
                .orderByDesc(SysDictionary::getSort)
                .orderByDesc(SysDictionary::getId);
        List<SysDictionary> dictionaryList = sqlToyHelperDao.findList(wrapper);
        // 查询关联的用户名称
        List<BigInteger> userIds = dictionaryList.stream().map(SysDictionary::getCreateBy).collect(Collectors.toList());
        List<SysUser> sysUsers = sqlToyHelperDao.loadByIds(SysUser.class, userIds);
        Map<BigInteger, String> userMap = sysUsers
                .stream()
                .collect(Collectors.toMap(SysUser::getId, i -> i.getUsername().concat(CharConst.AT).concat(i.getNickname())));
        List<SysDictionaryVO> dictionaryVOList = new ArrayList<>(dictionaryList.size());
        for (SysDictionary dictionary : dictionaryList) {
            SysDictionaryVO dictionaryVO = Convert.convert(SysDictionaryVO.class, dictionary);
            if (dictionary.getCreateBy() != null) {
                String username = userMap.get(dictionary.getCreateBy());
                dictionaryVO.setCreateBy(username);
            }
            dictionaryVOList.add(dictionaryVO);
        }
        return dictionaryVOList;
    }

    @Override
    public boolean delete(BigInteger id) {
        SysDictionary sysDictionary = new SysDictionary().setId(id).setIsDeleted(IsEnum.Y.getCode());
        Long update = sqlToyHelperDao.update(sysDictionary);
        if (update <= 0) {
            return false;
        }
        // 获取子节点
        LambdaQueryWrapper<SysDictionary> wrapper = new LambdaQueryWrapper<>(SysDictionary.class)
                .select()
                .eq(SysDictionary::getIsDeleted, IsEnum.N.getCode())
                .eq(SysDictionary::getParentId, id);
        List<SysDictionary> list = sqlToyHelperDao.findList(wrapper);
        if (!list.isEmpty()) {
            // 存在子节点，删除子节点
            list.forEach(item -> delete(item.getId()));
        }
        return true;
    }

}
