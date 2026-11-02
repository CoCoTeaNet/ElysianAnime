package net.cocotea.elysiananime.util;

import org.sagacity.sqltoy.plugins.id.IdGenerator;
import org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator;

import java.math.BigInteger;
import java.util.Date;

/**
 * 雪花ID生成。
 * <p>
 * 新表（ani_media / ani_user_opus_progress / ani_opus_update）的 id 都是无自增的 bigint，
 * 雪花只在 sqltoy 的 save() 路径生效，手写 SQL 必须自己生成 id。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
public final class SnowflakeIds {

    private SnowflakeIds() {
    }

    public static BigInteger nextId() {
        IdGenerator generator = SnowflakeIdGenerator.getInstance();
        Object id = generator.getId(null, null, null, null, new Date(), null, 0, 0);
        return BigInteger.valueOf(((Number) id).longValue());
    }
}
