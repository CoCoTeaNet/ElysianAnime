package net.cocotea.janime.common.model;

import cn.hutool.core.convert.Convert;
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

    private Long pageNo;
    private Long pageSize;
    private List<T> records;
    private Long total;

    private ApiPage(Page<T> sourcePage, Class<T> elementType) {
        this.pageNo = sourcePage.getPageNo();
        this.pageSize = (long) sourcePage.getPageSize();
        this.total = sourcePage.getRecordCount();
        this.records = Convert.toList(elementType, sourcePage.getRows());
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

}
