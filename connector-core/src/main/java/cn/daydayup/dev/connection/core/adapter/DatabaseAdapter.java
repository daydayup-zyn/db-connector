package cn.daydayup.dev.connection.core.adapter;

import cn.daydayup.dev.connection.core.DataSource;
import cn.daydayup.dev.connection.core.utils.Configuration;
import cn.daydayup.dev.connection.core.utils.loader.ClassLoaderSwapper;
import cn.daydayup.dev.connection.core.utils.loader.LoadUtil;

/**
 * @ClassName DatabaseAdapter
 * @Description 数据库适配器
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:33
 * @Version 1.0
 */
public class DatabaseAdapter implements Adapter {

    private String config;
    private static ClassLoaderSwapper classLoaderSwapper = ClassLoaderSwapper
            .newCurrentThreadClassLoaderSwapper();
    private DatabaseAdapter(){

    }
    public static DatabaseAdapter getAdapter(){
        return Holder.instance;
    }

    private static class Holder{
        private static DatabaseAdapter instance = new DatabaseAdapter();
    }

    @Override
    public DataSource getDataSource(){
        Configuration conf = Configuration.from(config);
        String adapterName = conf.getString("type");
        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(adapterName));
        DataSource dataSource = LoadUtil.getDataSourcePlugin(adapterName);
        dataSource.init(conf);
        classLoaderSwapper.restoreCurrentThreadClassLoader();
        return dataSource;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
