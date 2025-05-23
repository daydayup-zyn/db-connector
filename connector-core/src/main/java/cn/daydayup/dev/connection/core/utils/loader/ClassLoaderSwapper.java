package cn.daydayup.dev.connection.core.utils.loader;

/**
 * @ClassName ClassLoaderSwapper
 * @Description 为避免jar冲突，比如hbase可能有多个版本的读写依赖jar包，JobContainer和TaskGroupContainer
 *              就需要脱离当前classLoader去加载这些jar包，执行完成后，又退回到原来classLoader上继续执行接下来的代码
 * @Author ZhaoYanNing
 * @Date 2025/4/19 13:43
 * @Version 1.0
 */
public class ClassLoaderSwapper {
    private ClassLoader storeClassLoader = null;

    private ClassLoaderSwapper() {
    }

    public static ClassLoaderSwapper newCurrentThreadClassLoaderSwapper() {
        return new ClassLoaderSwapper();
    }

    /**
     * 保存当前classLoader，并将当前线程的classLoader设置为所给classLoader
     *
     * @param classLoader 类加载器
     * @author zhanghuichao
     * @date 2020/01/28 15:08
     */
    public void setCurrentThreadClassLoader(ClassLoader classLoader) {
        this.storeClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    /**
     * 将当前线程的类加载器设置为保存的类加载
     */
    public void restoreCurrentThreadClassLoader() {
        Thread.currentThread().setContextClassLoader(this.storeClassLoader);
    }
}
