package cn.daydayup.dev.connection.test;

import cn.daydayup.dev.connection.core.adapter.DatabaseAdapter;
import cn.daydayup.dev.connection.core.database.AbstractJdbcDataSource;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

/**
 * @ClassName MysqlTest
 * @Description mysql连接器测试
 * @Author ZhaoYanNing
 * @Date 2024/4/18 18:35
 * @Version 1.0
 */
public class MysqlTest {

    public static void main(String[] args) {
        String config = """
                {
                    "username": "root",
                    "password": "123456",
                    "type": "mysql",
                    "host": "10.8.10.182",
                    "port": "33068",
                    "schema": "chat_model",
                    "driver-class-name": "com.mysql.cj.jdbc.Driver",
                    "jdbcUrl": "jdbc:mysql://10.8.10.182:33068/chat_model?useSSL=false"
                }
                """;
        DatabaseAdapter adapter = DatabaseAdapter.getAdapter();
        adapter.setConfig(config);
        AbstractJdbcDataSource dataSource = (AbstractJdbcDataSource) adapter.getDataSource();
        Pair<List<String>, List<List<String>>> allTables = dataSource.getAllTableInfo();
        System.out.println(JSON.toJSONString(allTables));
    }
}
