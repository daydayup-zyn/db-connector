package cn.daydayup.dev.connection.core.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @ClassName DatabaseConnection
 * @Description 数据库连接
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:34
 * @Version 1.0
 */
@FunctionalInterface
public interface DatabaseConnection {
    /**
     * 获取数据库连接
     * @return Connection
     * @throws SQLException
     */
    Connection getConnection() throws Exception;
}
