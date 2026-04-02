package net.cocotea.elysiananime.handler;

import cn.dev33.satoken.json.SaJsonTemplate;
import com.alibaba.fastjson2.JSON;
import org.noear.solon.annotation.Component;

import java.util.Map;

@Component
public class SaJsonTemplateImpl implements SaJsonTemplate {
    @Override
    public String objectToJson(Object obj) {
        return JSON.toJSONString( obj);
    }

    @Override
    public <T> T jsonToObject(String jsonStr, Class<T> type) {
        return JSON.parseObject(jsonStr, type);
    }

    @Override
    public Object jsonToObject(String jsonStr) {
        return SaJsonTemplate.super.jsonToObject(jsonStr);
    }

    @Override
    public Map<String, Object> jsonToMap(String jsonStr) {
        return SaJsonTemplate.super.jsonToMap(jsonStr);
    }
}
