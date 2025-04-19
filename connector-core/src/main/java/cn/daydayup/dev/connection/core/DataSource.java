package cn.daydayup.dev.connection.core;

import cn.daydayup.dev.connection.core.utils.Configuration;

/**
 * @ClassName DataSource
 * @Description 数据源抽象接口
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:31
 * @Version 1.0
 */
public interface DataSource {

    /**
     * 数据源配置初始化
     * @param conf
     */
    void init(Configuration conf);
}
