package cn.daydayup.dev.connection.test;

import cn.daydayup.dev.connection.core.adapter.DatabaseAdapter;
import cn.daydayup.dev.connection.core.database.AbstractJdbcDataSource;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @ClassName OracleTest
 * @Description Oracle连接器测试
 * @Author ZhaoYanNing
 * @Date 2025/4/24 14:15
 * @Version 1.0
 */
public class OracleTest {

    public static void main(String[] args) {
        String config = """
                {
                    "username": "us_yzt",
                    "password": "yzt1",
                    "type": "oracle",
                    "host": "10.8.10.184",
                    "port": "1521",
                    "schema": "ORCL",
                    "space":"US_APP",
                    "driver-class-name": "oracle.jdbc.OracleDriver",
                    "jdbcUrl": "jdbc:oracle:thin:@10.8.10.184:1521:ORCL"
                }
                """;
        DatabaseAdapter adapter = DatabaseAdapter.getAdapter();
        adapter.setConfig(config);
        AbstractJdbcDataSource dataSource = (AbstractJdbcDataSource) adapter.getDataSource();
        Pair<List<String>, List<List<String>>> allTables = dataSource.getAllTableInfo();
        System.out.println(JSON.toJSONString(allTables));
    }
}
