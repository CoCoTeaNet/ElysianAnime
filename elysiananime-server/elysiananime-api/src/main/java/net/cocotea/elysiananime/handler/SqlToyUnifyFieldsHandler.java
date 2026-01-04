package net.cocotea.elysiananime.handler;

import cn.dev33.satoken.stp.StpUtil;
import org.sagacity.sqltoy.model.DataAuthFilterConfig;
import org.sagacity.sqltoy.model.IgnoreCaseSet;
import org.sagacity.sqltoy.model.IgnoreKeyCaseMap;
import org.sagacity.sqltoy.plugins.IUnifyFieldsHandler;
import org.sagacity.sqltoy.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @date 2022-1-14 23:08:02
 * @author CoCoTea
 */
public class SqlToyUnifyFieldsHandler implements IUnifyFieldsHandler {
    private static final Logger logger = LoggerFactory.getLogger(SqlToyUnifyFieldsHandler.class);

    private String getLoginId() {
        String loginId;
        try {
            loginId  = (String) StpUtil.getLoginId();
        } catch (Exception e) {
            logger.info("warning!func[getLoginId],add this item not login.");
            loginId = null;
        }
        return loginId;
    }

    @Override
    public Map<String, Object> createUnifyFields() {
        LocalDateTime nowTime = DateUtil.getDateTime();
        String loginId = getLoginId();
        Map<String, Object> map = new HashMap<>(6);
        if (loginId != null) {
            map.put("createBy", loginId);
            map.put("updateBy", loginId);
        }
        map.put("createTime", nowTime);
        map.put("updateTime", nowTime);
        map.put("deleteStatus", 1);
        map.put("sort", 1);
        return map;
    }

    @Override
    public Map<String, Object> updateUnifyFields() {
        LocalDateTime nowTime = DateUtil.getDateTime();
        String loginId = getLoginId();
        Map<String, Object> map = new HashMap<>(2);
        if (loginId != null) {
            map.put("updateBy", loginId);
        }
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
