package net.cocotea.elysiananime.common.service;

import net.cocotea.elysiananime.common.model.BusinessException;

import java.math.BigInteger;
import java.util.List;

/**
 * 增删改查基础服务 接口
 * @param <T> 分页返回对象
 * @param <S> 分页对象
 * @param <A> 新增参数
 * @param <U> 更新参数
 * @author jwss
 * @date 2022-3-23 13:49:17
 */
public interface BaseService<T, S, A, U> {

    /**
     * 新增字典
     * @param param 新增参数
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    boolean add(A param) throws BusinessException;

    /**
     * 批量删除
     * @param idList id集合
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    boolean deleteBatch(List<BigInteger> idList) throws BusinessException;

    /**
     * 更新信息
     * @param param 更新参数
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    boolean update(U param) throws BusinessException;

    /**
     * 分页获取
     * @param param 分页参数
     * @return 列表
     * @throws BusinessException 业务异常
     */
    T listByPage(S param) throws BusinessException;

    /**
     * 移除单个
     * @param id 主键id
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    boolean delete(BigInteger id) throws BusinessException;
}
