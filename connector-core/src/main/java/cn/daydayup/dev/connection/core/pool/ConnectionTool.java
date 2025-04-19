package cn.daydayup.dev.connection.core.pool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @ClassName ConnectionTool
 * @Description 连接工具类
 * @Author ZhaoYanNing
 * @Date 2025/4/19 12:38
 * @Version 1.0
 */
public class ConnectionTool {

    /**
     * 关闭连接
     * @param conn 连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭数据库资源
     * @param conn 连接
     * @param rs 结果集
     * @param stmt Statement对象
     */
    public static void closeDBResource(final Connection conn, final ResultSet rs, final Statement stmt) {
        try {
            if (null != rs) {
                rs.close();
            }
            if (null != stmt) {
                stmt.close();
            }
            if (null != conn) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
