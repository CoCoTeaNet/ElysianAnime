package net.cocotea.janime.common.model;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.model.Page;

import java.util.List;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class ApiPage<T> {
    private ApiPage() {
    }

    /**
     * 页码
     */
    private Long pageNo;

    /**
     * 页大小
     */
    private Long pageSize;

    /**
     * 列表数据
     */
    private List<T> records;

    /**
     * 总数
     */
    private Long total;

    private ApiPage(Page<T> sourcePage, Class<T> elementType) {
        this.pageNo = sourcePage.getPageNo();
        this.pageSize = (long) sourcePage.getPageSize();
        this.total = sourcePage.getRecordCount();
        this.records = BeanUtil.copyToList(sourcePage.getRows(), elementType);
    }

    private ApiPage(Page<T> sourcePage, List<T> rows) {
        this.pageNo = sourcePage.getPageNo();
        this.pageSize = (long) sourcePage.getPageSize();
        this.total = sourcePage.getRecordCount();
        this.records = rows;
    }

    private ApiPage(Page<T> sourcePage) {
        this.pageNo = sourcePage.getPageNo();
        this.pageSize = (long) sourcePage.getPageSize();
        this.total = sourcePage.getRecordCount();
        this.records = sourcePage.getRows();
    }

    public static <T> ApiPage<T> rest(Object sourcePage, List<T> rows) {
        @SuppressWarnings("unchecked")
        Page<T> page = (Page<T>) sourcePage;
        return new ApiPage<>(page, rows);
    }

    public static <T> ApiPage<T> rest(Page<T> sourcePage) {
        return new ApiPage<>(sourcePage);
    }

    public static <T> ApiPage<T> rest(Object sourcePage, Class<T> elementType) {
        @SuppressWarnings("unchecked")
        Page<T> page = (Page<T>) sourcePage;
        return new ApiPage<>(page, elementType);
    }

    public static <T> Page<T> create(ApiPageDTO pageDTO) {
        Page<T> page = new Page<>();
        page.setPageNo(pageDTO.getPageNo());
        page.setPageSize(pageDTO.getPageSize());
        return page;
    }

}
