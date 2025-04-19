package cn.daydayup.dev.connection.core.database;

import cn.daydayup.dev.connection.core.DataSource;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;
import java.util.Map;

/**
 * @ClassName JdbcDataSource
 * @Description 数据库连接
 * @Author ZhaoYanNing
 * @Date 2025/4/19 14:34
 * @Version 1.0
 */
public interface JdbcDataSource extends DataSource {

    /**
     * 获取表数据
     *
     * @param query 查询语句
     * @return Pair leftlist->列名,rightlist->表数据
     */
    Pair<List<String>, List<List<String>>> query(String query);

    /**
     * 获取表元数据信息
     *
     * @param tableName 选择的table
     * @return left->["COLUMN_NAME","COLUMN_TYPE","COLUMN_LENGTH","COLUMN_SCALE","IS_NULLABLE","PRIMARY_KEY","COLUMN_COMMENT"]
     */
    Pair<List<String>, List<List<String>>> getColumnInfo(String tableName);

    /**
     * 获取指定数据库中所有表的详细信息（表名、表注释、表类型、表空间、拥有者、是否临时表、是否系统表）
     * @return left -> ["TABLE_NAME","TABLE_COMMENT","TABLE_TYPE","TABLE_SPACE","TABLE_OWNER","IS_TEMPORARY_TABLE","IS_SYSTEM_TABLE"]
     */
    Pair<List<String>, List<List<String>>> getAllTableInfo();
}
