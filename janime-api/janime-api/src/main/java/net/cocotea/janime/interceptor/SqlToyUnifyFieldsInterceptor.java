package net.cocotea.janime.interceptor;

import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.util.LoginUtils;
import org.sagacity.sqltoy.model.DataAuthFilterConfig;
import org.sagacity.sqltoy.model.IgnoreCaseSet;
import org.sagacity.sqltoy.model.IgnoreKeyCaseMap;
import org.sagacity.sqltoy.plugins.IUnifyFieldsHandler;
import org.sagacity.sqltoy.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 2.0.0
 * @author CoCoTea
 */
public class SqlToyUnifyFieldsInterceptor implements IUnifyFieldsHandler {
    private static final Logger logger = LoggerFactory.getLogger(SqlToyUnifyFieldsInterceptor.class);

    @Override
    public Map<String, Object> createUnifyFields() {
        LocalDateTime nowTime = DateUtil.getDateTime();
        BigInteger loginId = LoginUtils.loginId();
        Map<String, Object> map = new HashMap<>(5);
        map.put("createBy", loginId);
        map.put("createTime", nowTime);
        map.put("updateBy", loginId);
        map.put("updateTime", nowTime);
        map.put("isDeleted", IsEnum.N.getCode());
        return map;
    }

    @Override
    public Map<String, Object> updateUnifyFields() {
        LocalDateTime nowTime = DateUtil.getDateTime();
        BigInteger loginId = LoginUtils.loginId();
        Map<String, Object> map = new HashMap<>(2);
        map.put("updateBy", loginId);
        map.put("updateTime", nowTime);
        return map;
    }

    @Override
    public IgnoreCaseSet forceUpdateFields() {
        return IUnifyFieldsHandler.super.forceUpdateFields();
    }

    @Override
    public IgnoreKeyCaseMap<String, DataAuthFilterConfig> dataAuthFilters() {
        return IUnifyFieldsHandler.super.dataAuthFilters();
    }
}
