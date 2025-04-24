package cn.daydayup.dev.connection.test;

import cn.daydayup.dev.connection.core.adapter.DatabaseAdapter;
import cn.daydayup.dev.connection.core.database.AbstractJdbcDataSource;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @ClassName PostgresqlTest
 * @Description TODO
 * @Author ZhaoYanNing
 * @Date 2025/4/24 14:58
 * @Version 1.0
 */
public class PostgresqlTest {

    public static void main(String[] args) {
        String config = """
                {
                    "username":"postgres",
                    "password":"postgres",
                    "type":"postgresql",
                    "host":"10.8.10.183",
                    "port":"5432",
                    "schema":"sxzn",
                    "space":"xinchangzhineng",
                    "driver-class-name":"org.postgresql.Driver",
                    "version":"","jdbcUrl":"jdbc:postgresql://10.8.10.183:5432/sxzn"
                }
                """;
        DatabaseAdapter adapter = DatabaseAdapter.getAdapter();
        adapter.setConfig(config);
        AbstractJdbcDataSource dataSource = (AbstractJdbcDataSource) adapter.getDataSource();
        Pair<List<String>, List<List<String>>> allTables = dataSource.getAllTableInfo();
        System.out.println(JSON.toJSONString(allTables));
    }
}
