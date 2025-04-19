package cn.daydayup.dev.connection.core.database;

import cn.daydayup.dev.connection.core.DataSource;
import cn.daydayup.dev.connection.core.utils.Configuration;

/**
 * @ClassName AbstractDataSource
 * @Description 数据源抽象类
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:28
 * @Version 1.0
 */
public abstract class AbstractDataSource implements DataSource {

    @Override
    public abstract void init(Configuration conf);
}
