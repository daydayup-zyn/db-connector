package cn.daydayup.dev.connection.core.database;

import cn.daydayup.dev.connection.core.constants.ConnectingPoolConstants;
import cn.daydayup.dev.connection.core.constants.DatabaseConstants;
import cn.daydayup.dev.connection.core.pool.ConnectionTool;
import cn.daydayup.dev.connection.core.pool.JdbcConnectionPool;
import cn.daydayup.dev.connection.core.utils.Configuration;
import cn.daydayup.dev.connection.core.utils.DataSourceKey;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.dbcp2.BasicDataSource;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * @ClassName AbstractJdbcDataSource
 * @Description 数据源抽象类
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:28
 * @Version 1.0
 */
public abstract class AbstractJdbcDataSource extends AbstractDataSource implements JdbcDataSource, DatabaseConnection {

    protected DataSource dataSource;

    protected Configuration dsConfig;

    /**
     * 初始化数据源配置
     *
     * @param conf 数据源配置JSON
     */
    @Override
    public void init(Configuration conf) {
        this.dsConfig = conf;
        Boolean usePool = conf.getBool(ConnectingPoolConstants.USE_POOL);
        if (Objects.nonNull(usePool) && usePool) {
            this.dataSource = JdbcConnectionPool.get(DataSourceKey.getKey(this.dsConfig));
            if (this.dataSource == null) {
                BasicDataSource basicDataSource = new BasicDataSource();
                basicDataSource.setDriverClassName(conf.getString(DatabaseConstants.DRIVER_NAME));
                basicDataSource.setUrl(conf.getString(DatabaseConstants.URL));
                basicDataSource.setUsername(conf.getString(DatabaseConstants.USERNAME));
                basicDataSource.setPassword(conf.getString(DatabaseConstants.PASSWORD));
                basicDataSource.setInitialSize(conf.getInt(ConnectingPoolConstants.INITIALSIZE) == null ? 3 : conf.getInt(ConnectingPoolConstants.INITIALSIZE));
                basicDataSource.setMaxTotal(conf.getInt(ConnectingPoolConstants.MAXTOTAL) == null ? 10 : conf.getInt(ConnectingPoolConstants.MAXTOTAL));
                basicDataSource.setMaxIdle(conf.getInt(ConnectingPoolConstants.MAXIDLE) == null ? 5 : conf.getInt(ConnectingPoolConstants.MAXIDLE));
                basicDataSource.setMinIdle(conf.getInt(ConnectingPoolConstants.MINIDLE) == null ? 1 : conf.getInt(ConnectingPoolConstants.MINIDLE));
                basicDataSource.setMaxWaitMillis(conf.getLong(ConnectingPoolConstants.MAXWAIT) == null ? 10000 : conf.getLong(ConnectingPoolConstants.MAXWAIT));
                basicDataSource.setDriverClassLoader(Thread.currentThread().getContextClassLoader());
                this.dataSource = basicDataSource;
                JdbcConnectionPool.set(DataSourceKey.getKey(this.dsConfig), this.dataSource);
            }
        }
    }

    @Override
    public Connection getConnection() throws Exception {
        return this.dataSource == null ? this.getSingleConnection() : this.dataSource.getConnection();
    }

    /**
     * 非链接池的连接放到具体插件中创建
     *
     * @return
     * @throws Exception
     */
    abstract protected Connection getSingleConnection() throws Exception;

    /**
     * 执行DML
     *
     * @param sql dml代码
     * @return
     */
    protected String manipulate(String sql) {
        Connection conn = null;
        Statement st = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            st = conn.createStatement();
            return String.valueOf(st.executeUpdate(sql));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ConnectionTool.closeDBResource(conn, null, st);
        }
    }

    /**
     * 字符串gzip压缩
     *
     * @param primStr 需要压缩的字符串
     * @return java.lang.String
     * @author fanyanyan
     * @date 2020/10/23 10:59
     */
    @SuppressWarnings("restriction")
    protected static String gzip(String primStr) {
        if (primStr == null || primStr.isEmpty()) {
            return primStr;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(primStr.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }


    /**
     * 友好的展示blob字段
     *
     * @param rs
     * @param i
     * @return java.lang.String
     * @author bly
     * @date 2021/6/23 17:32
     */
    protected String handleBlob(ResultSet rs, int i) {
        try {
            Blob blob = rs.getBlob(i);
            if (blob == null) {
                return null;
            }
            int length = (int) blob.length();
            String blobString = new String(blob.getBytes(1, length), StandardCharsets.ISO_8859_1);
            blobString = gzip(blobString);
            return "(blob)" + blob.length() + "byte" + " |-| " + blobString;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
