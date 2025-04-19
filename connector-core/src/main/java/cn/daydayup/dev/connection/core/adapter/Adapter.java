package cn.daydayup.dev.connection.core.adapter;

import cn.daydayup.dev.connection.core.DataSource;

/**
 * @ClassName Adapter
 * @Description 数据源适配器
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:33
 * @Version 1.0
 */
@FunctionalInterface
public interface Adapter{
    /**
     * 获取数据源
     * @return DataSource 数据源实例
     */
    DataSource getDataSource();
}
