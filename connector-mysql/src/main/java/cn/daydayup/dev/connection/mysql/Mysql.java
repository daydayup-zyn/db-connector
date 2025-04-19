package cn.daydayup.dev.connection.mysql;

import cn.daydayup.dev.connection.core.constants.DatabaseConstants;
import cn.daydayup.dev.connection.core.database.AbstractJdbcDataSource;
import cn.daydayup.dev.connection.core.pool.ConnectionTool;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

/**
 * @ClassName Mysql
 * @Description MySQL连接器
 * @Author ZhaoYanNing
 * @Date 2025/4/19 15:12
 * @Version 1.0
 */
public class Mysql extends AbstractJdbcDataSource {
    @Override
    protected  Connection getSingleConnection() throws Exception {
        Class.forName(this.dsConfig.getString(DatabaseConstants.DRIVER_NAME));
        Properties props = new Properties();
        props.setProperty("user", this.dsConfig.getString(DatabaseConstants.USERNAME));
        if (StringUtils.isNotBlank(this.dsConfig.getString(DatabaseConstants.PASSWORD))) {
            props.setProperty("password", this.dsConfig.getString(DatabaseConstants.PASSWORD));
        }
        return DriverManager.getConnection(this.dsConfig.getString(DatabaseConstants.URL), props);
    }

    @Override
    public Pair<List<String>, List<List<String>>> query(String query) {
        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            MutablePair<List<String>, List<List<String>>> tableDate = new MutablePair<>();
            String[] test = {"drop", "delete", "insert", "update", "create", "alter"};
            for (String exe : test) {
                String regex = "(" + exe + ").*";
                if (query.toLowerCase().matches(regex)) {
                    String execute = manipulate(query);
                    ArrayList<String> left = new ArrayList<>();
                    left.add("other");
                    tableDate.setLeft(left);
                    ArrayList<List<String>> right = new ArrayList<>();
                    ArrayList<String> innerRight = new ArrayList<>();
                    innerRight.add(execute);
                    right.add(innerRight);
                    tableDate.setRight(right);
                    return tableDate;
                }
            }
            conn = getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            ArrayList<String> columnLabelNameList = new ArrayList<>(columnCount);
            ArrayList<String> columnTypeNameList = new ArrayList<>(columnCount);
            ArrayList<List<String>> rows = new ArrayList<>();

            for (int i = 1; i < columnCount + 1; i++) {
                String columnLabel = metaData.getColumnLabel(i);
                String columnTypeName = metaData.getColumnTypeName(i);
                {
                    columnLabelNameList.add(columnLabel);
                    columnTypeNameList.add(columnTypeName);
                }
            }
            while (rs.next()) {
                ArrayList<String> row = new ArrayList<>();
                for (int i = 1; i < columnCount + 1; i++) {
                    // 增加对于blob字段的支持,暂存blob字段信息在项目路径中-fanyanyan
                    String colType = columnTypeNameList.get(i - 1);
                    if ("longblob".equalsIgnoreCase(colType) || "blob".equalsIgnoreCase(colType)) {
                        String rowString = handleBlob(rs, i);
                        row.add(rowString);
                    } else {
                        String columnData;
                        if (columnTypeNameList.get(i - 1).equalsIgnoreCase("year")) {
                            columnData = rs.getInt(i) + "";
                        } else if (columnTypeNameList.get(i - 1).equalsIgnoreCase("double")) {
                            columnData = new BigDecimal(rs.getDouble(i) + "").toString();
                        } else {
                            columnData = rs.getString(i);
                        }
                        row.add(columnData == null ? "" : columnData);
                    }
                }
                rows.add(row);
            }
            tableDate.setLeft(columnLabelNameList);
            tableDate.setRight(rows);
            return tableDate;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ConnectionTool.closeDBResource(conn, rs, st);
        }
    }

    @Override
    public Pair<List<String>, List<List<String>>> getColumnInfo(String tableName) {
        String dbName = getDatabaseName();
        String getColumnInfoSql = """
                SELECT
                 COLUMN_NAME,
                 DATA_TYPE AS COLUMN_TYPE,
                 COALESCE(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) AS COLUMN_LENGTH,
                 NUMERIC_SCALE AS COLUMN_SCALE,
                 IS_NULLABLE,
                 CASE COLUMN_KEY WHEN 'PRI' THEN 'YES' ELSE COLUMN_KEY END PRIMARY_KEY,
                 COLUMN_COMMENT
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = '{tableName}'
                 AND TABLE_SCHEMA = '{dbName}'
                ORDER BY ORDINAL_POSITION;
                """;
        String columnInfoSql = getColumnInfoSql
                .replace("{tableName}", tableName)
                .replace("{dbName}", dbName);
        return query(columnInfoSql);
    }

    @Override
    public Pair<List<String>, List<List<String>>> getAllTableInfo() {
        String dbName = getDatabaseName();
        String getTableInfoSql = """
                select
                 TABLE_NAME,
                 TABLE_COMMENT
                from information_schema.tables where table_type = 'BASE TABLE' AND table_schema = '{dbName}';
                """;
        String tableInfoSql = getTableInfoSql.replace("{dbName}", dbName);
        return query(tableInfoSql);
    }

    private String getDatabaseName() {
        String[] tmp =  this.dsConfig.getString(DatabaseConstants.URL).split("\\?")[0].split("/");
        return tmp[tmp.length - 1];
    }

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
     * 友好的展示blob字段
     *
     * @param rs
     * @param i
     * @return java.lang.String
     */
    protected String handleBlob(ResultSet rs, int i) {
        try {
            Blob blob = rs.getBlob(i);
            if (blob == null) {
                return null;
            }
            int length = (int) blob.length();
            String blobString = new String(blob.getBytes(1, length), CharEncoding.ISO_8859_1);
            blobString = gzip(blobString);
            return "(blob)" + blob.length() + "byte" + " |-| " + blobString;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串gzip压缩
     *
     * @param primStr 需要压缩的字符串
     * @return java.lang.String
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
}
