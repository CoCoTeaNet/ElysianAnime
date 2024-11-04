package net.cocotea.elysiananime.client;

import com.dtflys.forest.annotation.ForestClient;
import com.dtflys.forest.annotation.Get;
import net.cocotea.elysiananime.client.result.calendar.CalendarResult;

import java.util.List;

/**
 * Bangumi客户端工具，接口来源：<a href="https://github.com/bangumi/api">Api</a>
 *
 * @author CoCoTea
 * @since v2.2.2
 */
@ForestClient
public interface BangumiClient {

    /**
     * 基础URL地址
     */
    String BASE_URL = "https://api.bgm.tv";

    /**
     * 每日放送
     *
     * @return {@link CalendarResult}
     */
    @Get(BASE_URL + "/calendar")
    List<CalendarResult> calendar();

}
