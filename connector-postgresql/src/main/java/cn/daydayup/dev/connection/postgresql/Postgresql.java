package cn.daydayup.dev.connection.postgresql;

import cn.daydayup.dev.connection.core.constants.DatabaseConstants;
import cn.daydayup.dev.connection.core.database.AbstractJdbcDataSource;
import cn.daydayup.dev.connection.core.pool.ConnectionTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @ClassName Postgresql
 * @Description Postgresql连接器
 * @Author ZhaoYanNing
 * @Date 2025/4/24 14:43
 * @Version 1.0
 */
public class Postgresql extends AbstractJdbcDataSource {
    @Override
    protected Connection getSingleConnection() throws SQLException, ClassNotFoundException {
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
            MutablePair<List<String>, List<List<String>>> tableDate = new MutablePair();
            String[] test = { "drop", "delete", "insert", "update", "create", "alter" };
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
                    return (Pair<List<String>, List<List<String>>>)tableDate;
                }
            }
            conn = getSingleConnection();
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
                columnLabelNameList.add(columnLabel);
                columnTypeNameList.add(columnTypeName);
            }
            while (rs.next()) {
                ArrayList<String> row = new ArrayList<>();
                for (int j = 1; j < columnCount + 1; j++) {
                    String colType = columnTypeNameList.get(j - 1);
                    if ("bytea".equalsIgnoreCase(colType) || "blob".equalsIgnoreCase(colType)) {
                        String rowString = handleBlob_t(rs, j);
                        row.add(rowString);
                    } else {
                        String columnData = rs.getString(j);
                        row.add((columnData == null) ? "" : columnData);
                    }
                }
                rows.add(row);
            }
            tableDate.setLeft(columnLabelNameList);
            tableDate.setRight(rows);
            return (Pair<List<String>, List<List<String>>>)tableDate;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            ConnectionTool.closeDBResource(conn, rs, st);
        }
    }

    private String handleBlob_t(ResultSet rs, int i) {
        try {
            Blob blob = rs.getBlob(i);
            if (blob == null){
                return null;
            }
            int length = (int)blob.length();
            String blobString = new String(blob.getBytes(1L, length), StandardCharsets.ISO_8859_1);
            blobString = gzip(blobString);
            return "(blob)" + blob.length() + "byte |-| " + blobString;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pair<List<String>, List<List<String>>> getColumnInfo(String tableName) {
        String getColumnInfoSql = """
                SELECT
                    att.attname AS field_name,
                    format_type(att.atttypid, att.atttypmod) AS field_type,
                    COALESCE(d.description, '') AS field_description
                FROM
                    pg_attribute att
                LEFT JOIN
                    pg_description d ON (d.objoid = att.attrelid AND d.objsubid = att.attnum)
                JOIN
                    pg_class c ON c.oid = att.attrelid
                JOIN
                    pg_namespace n ON n.oid = c.relnamespace
                WHERE
                    c.relname = '{tableName}' AND att.attnum > 0
                ORDER BY
                    att.attnum;
                """;
        String columnInfoSql = getColumnInfoSql
                .replace("{tableName}", tableName);
        return query(columnInfoSql);
    }

    @Override
    public Pair<List<String>, List<List<String>>> getAllTableInfo() {
        String dbName = getDatabaseName();
        String getTableInfoSql = """
                SELECT
                    n.nspname,
                    c.relname AS table_name,
                    d.description AS table_description
                FROM
                    pg_class c
                JOIN
                    pg_namespace n ON n.oid = c.relnamespace
                LEFT JOIN
                    pg_description d ON d.objoid = c.oid AND d.objsubid = 0
                WHERE
                    c.relkind = 'r'
                    AND n.nspname = '{dbName}'
                ORDER BY
                    n.nspname, c.relname;
                """;
        String tableInfoSql = getTableInfoSql.replace("{dbName}", dbName);
        return query(tableInfoSql);
    }

    private String getDatabaseName() {
        return this.dsConfig.getString(DatabaseConstants.SPACE);
    }
}
