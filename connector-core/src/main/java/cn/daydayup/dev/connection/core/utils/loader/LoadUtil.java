package cn.daydayup.dev.connection.core.utils.loader;

import cn.daydayup.dev.connection.core.database.AbstractDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName LoadUtil
 * @Description Jar包加载工具类
 * @Author ZhaoYanNing
 * @Date 2025/4/19 13:57
 * @Version 1.0
 */
public class LoadUtil {

    private static String pathSeparator = "/";

    /**
     * 目标类全限定名头
     */
    private static String classPath = "cn.daydayup.dev.connection";

    /**
     * jarLoader的缓冲
     */
    private static Map<String, JarLoader> jarLoaderCenter = new ConcurrentHashMap<>();

    public static AbstractDataSource getDataSourcePlugin(String datasourceType) {
        Class<? extends AbstractDataSource> clazz = loadDataSourcePluginClass(datasourceType);
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AbstractDataSource getDataSourcePlugin(String datasourceType, BasicDataSource basicDataSource) {
        Class<? extends AbstractDataSource> clazz = loadDataSourcePluginClass(datasourceType);
        try {
            final Constructor<? extends AbstractDataSource> constructor = clazz.getDeclaredConstructor(BasicDataSource.class);
            if (!constructor.isAccessible()){
                constructor.setAccessible(true);
            }
            final AbstractDataSource databaseSource = constructor.newInstance(basicDataSource);
            constructor.setAccessible(false);
            return databaseSource;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static synchronized JarLoader getJarLoader(String datasourceType) {
        JarLoader jarLoader = jarLoaderCenter.get(datasourceType);
        if (jarLoader == null) {
            String pluginPath = System.getProperty("plugin.dir");
            if (Objects.isNull(pluginPath)){
                // 获取当前项目根路径
                pluginPath = System.getProperty("user.dir");
                if (Objects.isNull(pluginPath)){
                    throw new RuntimeException("不存在有效路径");
                }
            }
            if (!pluginPath.endsWith(pathSeparator)) {
                pluginPath += "/";
            }
            String jarPath = pluginPath + datasourceType;
            jarLoader = new JarLoader(new String[]{jarPath});
            jarLoaderCenter.put(datasourceType, jarLoader);
        }
        return jarLoader;
    }

    /**
     * 反射出具体的DataSource实例
     *
     * @param datasourceType    数据源类型
     * @return
     */
    @SuppressWarnings("unchecked")
    private static synchronized Class<? extends AbstractDataSource> loadDataSourcePluginClass(
            String datasourceType) {
        JarLoader jarLoader = getJarLoader(datasourceType);
        try {
            String className = datasourceType;
            String classFullName = classPath + "." + datasourceType + "." + uppercaseFirstLetter(className);
            return (Class<? extends AbstractDataSource>) jarLoader.loadClass(classFullName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private static String uppercaseFirstLetter(String str) {
        StringBuilder sb = new StringBuilder();
        String firstLetter = str.substring(0, 1);
        String uppercaseLetter = firstLetter.toUpperCase();
        return sb.append(uppercaseLetter).append(str.substring(1)).toString();
    }
}
