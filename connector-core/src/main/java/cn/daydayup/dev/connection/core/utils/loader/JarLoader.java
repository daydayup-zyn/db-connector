package cn.daydayup.dev.connection.core.utils.loader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName JarLoader
 * @Description Jar包加载机制
 * @Author ZhaoYanNing
 * @Date 2025/4/19 13:45
 * @Version 1.0
 */
class JarLoader extends URLClassLoader {

    JarLoader(String[] paths) {
        this(paths, JarLoader.class.getClassLoader());
    }

    private JarLoader(String[] paths, ClassLoader parent) {
        super(getURLs(paths), parent);
    }

    private static URL[] getURLs(String[] paths) {
        Validate.isTrue(null != paths && 0 != paths.length,
                "jar包路径不能为空.");

        List<String> dirs = new ArrayList<String>();
        for (String path : paths) {
            dirs.add(path);
            JarLoader.collectDirs(path, dirs);
        }

        List<URL> urls = new ArrayList<URL>();
        for (String path : dirs) {
            urls.addAll(doGetURLs(path));
        }

        return urls.toArray(new URL[0]);
    }

    private static void collectDirs(String path, List<String> collector) {
        if (null == path || StringUtils.isBlank(path)) {
            return;
        }

        File current = new File(path);
        if (!current.exists() || !current.isDirectory()) {
            return;
        }

        if (current.listFiles() == null) {
            return;
        }

        for (File child : Objects.requireNonNull(current.listFiles())) {
            if (!child.isDirectory()) {
                continue;
            }

            collector.add(child.getAbsolutePath());
            collectDirs(child.getAbsolutePath(), collector);
        }
    }

    private static List<URL> doGetURLs(final String path) {
        Validate.isTrue(!StringUtils.isBlank(path), "jar包路径不能为空.");

        File jarPath = new File(path);

        Validate.isTrue(jarPath.exists() && jarPath.isDirectory(),
                String.format("jar包路径必须存在且为目录，当前设置的路径为%s", jarPath));

        /* set filter */
        FileFilter jarFilter = pathname -> pathname.getName().endsWith(".jar");

        /* iterate all jar */
        File[] allJars = new File(path).listFiles(jarFilter);
        List<URL> jarURLs = new ArrayList<URL>(allJars.length);

        for (File allJar : allJars) {
            try {
                jarURLs.add(allJar.toURI().toURL());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return jarURLs;
    }
}
