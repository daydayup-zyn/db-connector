package cn.daydayup.dev.connection.core.constants;

/**
 * @ClassName ConnectingPoolConstants
 * @Description 数据库连接池参数
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:30
 * @Version 1.0
 */
public class ConnectingPoolConstants {

    /**
     * 是否使用连接池
     */
    public static final String USE_POOL = "usePool";
    /**
     * 连接池启动时创建的初始化连接数量
     */
    public static final String INITIALSIZE = "initialSize";
    /**
     * 连接池在同一时间能够分配的最大活动连接的数量
     */
    public static final String MAXTOTAL = "maxTotal";
    /**
     * 连接池中容许保持空闲状态的最大连接数量,超过的空闲连接将被释放
     */
    public static final String MAXIDLE = "maxIdle";
    /**
     * 连接池中容许保持空闲状态的最小连接数量,低于这个数量将创建新的连接
     */
    public static final String MINIDLE = "minIdle";
    /**
     * 当没有可用连接时,连接池等待连接被归还的最大时间(以毫秒计数),超过时间则抛出异常,如果设置为-1表示无限等待
     */
    public static final String MAXWAIT = "maxWait";

}
