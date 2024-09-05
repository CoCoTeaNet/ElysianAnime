package net.cocotea.elysiananime.common.service;

/**
 * 增删改查基础服务 接口（增加详细方法）
 *
 * @param <T> 分页返回对象
 * @param <S> 分页对象
 * @param <A> 新增参数
 * @param <U> 更新参数
 * @author CoCoTea
 * @version 2.0.0
 */
public interface DetailService<T, S, A, U, D> extends BaseService<T, S, A, U> {

    /**
     * 获取详细
     *
     * @return 详细数据VO
     */
    D getDetail();

}
