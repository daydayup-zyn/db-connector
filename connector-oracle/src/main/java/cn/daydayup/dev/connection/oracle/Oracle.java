package cn.daydayup.dev.connection.oracle;

import cn.daydayup.dev.connection.core.constants.DatabaseConstants;
import cn.daydayup.dev.connection.core.database.AbstractJdbcDataSource;
import cn.daydayup.dev.connection.core.pool.ConnectionTool;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @ClassName Oracle
 * @Description Oracle连接器
 * @Author ZhaoYanNing
 * @Date 2025/4/24 13:52
 * @Version 1.0
 */
public class Oracle extends AbstractJdbcDataSource {
    @Override
    protected Connection getSingleConnection() throws Exception {
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

        try {//执行dml ddl 走的方法
            query = query.trim().replaceAll(";", "");
            MutablePair<List<String>, List<List<String>>> tableDate = new MutablePair<>();
            String[] test = {"drop", "delete", "insert", "update", "create", "alter", "comment"};
            for (String exe : test) {
                String regex = "(" + exe + ").*";
                if (query.toLowerCase().matches(regex) && !query.matches("^select$")) {
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
            ArrayList<List<String>> rows = new ArrayList<List<String>>();

            for (int i = 1; i < columnCount + 1; i++) {
                String columnLabel = metaData.getColumnLabel(i);
                String columnTypeName = metaData.getColumnTypeName(i);
                columnLabelNameList.add(columnLabel);
                columnTypeNameList.add(columnTypeName);
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
                        String columnData = rs.getString(i);
                        row.add(columnData);
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
                    a.column_name,
                    a.data_type,
                	b.comments
                FROM all_tab_columns a left join all_col_comments b
                    on a.COLUMN_NAME=b.COLUMN_NAME
                WHERE a.table_name = '{tableName}' and b.table_name = '{tableName}'
                    and a.owner = '{dbName}' and b.owner = '{dbName}';
                """;
        String columnInfoSql = getColumnInfoSql
                .replace("{dbName}", dbName)
                .replace("{tableName}", tableName);
        return query(columnInfoSql);
    }

    @Override
    public Pair<List<String>, List<List<String>>> getAllTableInfo() {
        String dbName = getDatabaseName();
        String getTableInfoSql = """
                SELECT
                    TABLE_NAME,
                    COMMENTS
                from all_tab_comments
                 where OWNER='{dbName}'
                order by TABLE_NAME
                """;
        String tableInfoSql = getTableInfoSql.replace("{dbName}", dbName);
        return query(tableInfoSql);
    }

    private String getDatabaseName() {
        return this.dsConfig.getString(DatabaseConstants.SPACE);
    }
}
