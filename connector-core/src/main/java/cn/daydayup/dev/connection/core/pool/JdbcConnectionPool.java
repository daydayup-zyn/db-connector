package cn.daydayup.dev.connection.core.pool;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName JdbcConnectionPool
 * @Description JDBC连接工具类
 * @Author ZhaoYanNing
 * @Date 2025/4/19 12:45
 * @Version 1.0
 */
public class JdbcConnectionPool {

    /**
     * 数据源缓存
     */
    private static final ConcurrentMap<String, DataSource> DATASOURCE_MAP = new ConcurrentHashMap<>();

    /**
     * 获取数据源
     * @param key 数据源key
     * @return 数据源对象
     */
    public static DataSource get(String key) {
        return DATASOURCE_MAP.get(key);
    }

    /**
     * 设置数据源
     * @param key 数据源key
     * @param dataSource 数据源对象
     */
    public static void set(String key, DataSource dataSource) {
        DataSource ds = DATASOURCE_MAP.get(key);
        if (ds == null) {
            synchronized (key.intern()) {
                ds = DATASOURCE_MAP.get(key);
                if (ds == null) {
                    DATASOURCE_MAP.put(key, dataSource);
                }
            }
        }
    }
}
