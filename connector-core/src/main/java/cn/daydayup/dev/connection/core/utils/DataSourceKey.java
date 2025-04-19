package cn.daydayup.dev.connection.core.utils;

import cn.daydayup.dev.connection.core.constants.DatabaseConstants;

/**
 * @ClassName DataSourceKey
 * @Description 数据库链接key
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:30
 * @Version 1.0
 */
public class DataSourceKey {

    public static String getKey(Configuration conf) {
        return conf.getString(DatabaseConstants.URL) +
                conf.getString(DatabaseConstants.USERNAME) +
                conf.getString(DatabaseConstants.PASSWORD);
    }
}
