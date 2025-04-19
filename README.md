# 数据源连接组件库 (db-connector)

## 项目简介

`db-connector` 是一个通用的数据源连接组件库，旨在为开发者提供灵活且高效的数据库连接管理能力。该项目通过模块化设计，支持多种数据库类型的插件式连接，并提供了丰富的数据库操作功能。

---

## 项目结构

项目采用 Maven 进行构建管理，包含以下主要模块：

### 1. `connector-core`
核心模块，提供数据源连接的基础功能和工具类。
- **目录**: `connector-core/src/main/java/cn/daydayup/dev/connection/core`
- **功能**:
    - 数据源抽象类 (`AbstractDataSource`, `AbstractJdbcDataSource`)
    - 数据库连接工具 (`DatabaseConnection`, `JdbcDataSource`)
    - 配置管理工具 (`Configuration`)
    - 连接池实现 (`JdbcConnectionPool`)

### 2. `connector-mysql`
MySQL 数据库连接的具体实现模块。
- **目录**: `connector-mysql/src/main/java/cn/daydayup/dev/connection/mysql`
- **功能**:
    - MySQL 数据源适配器 (`Mysql`)

### 3. `connector-test`
测试模块，用于验证核心功能的正确性。
- **目录**: `connector-test/src/main/java/cn/daydayup/dev/connection/test`
- **功能**:
    - 数据源连接测试 (`MysqlTest`)


---

## 依赖关系

### Maven 依赖
项目使用 Maven 进行依赖管理，以下是主要依赖项：
- `org.apache.commons:commons-lang3==3.10`: 工具类库。
- `org.apache.commons:commons-dbcp2==2.6`: 数据库连接池实现。
- `com.alibaba:fastjson==2.0`: JSON 解析和序列化。
- `commons-codec:commons-codec==1.11`: 编码工具。
- `mysql:mysql-connector-java==8.0`: MySQL 数据库驱动。

### 模块间依赖
- `connector-core` 是其他模块的基础，提供了核心功能。
- `connector-mysql` 依赖于 `connector-core`，实现了 MySQL 数据源的具体功能。
- `connector-test` 依赖于 `connector-core`，用于测试核心功能。

---

## 快速开始

### 1. 构建项目
确保已安装 JDK 17 和 Maven，然后执行以下命令：
```bash
mvn clean install
```

### 2. 使用示例

### 2.1 添加依赖
```xml
<dependency>
    <groupId>cn.daydayup.dev</groupId>
    <artifactId>connector-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2.2 添加MySQL依赖包
在项目根目录创建`mysql`文件夹，将`mysql-jar-with-dependencies.jar`放入其中。如需连接其他数据库，则需创建其数据库类型的文件夹`xxx`，并将`xxx-jar-with-dependencies.jar`放入其中。

### 2.3 以下是一个简单的使用示例：

```java
import cn.daydayup.dev.connection.core.adapter.DatabaseAdapter;
import cn.daydayup.dev.connection.core.database.AbstractJdbcDataSource;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public class Example { 
    public static void main(String[] args) {
      String config = """
              {
                  "username": "root",
                  "password": "123456",
                  "type": "mysql",
                  "host": "10.8.10.xxx",
                  "port": "3306",
                  "schema": "xxx",
                  "driver-class-name": "com.mysql.cj.jdbc.Driver",
                  "jdbcUrl": "jdbc:mysql://10.8.10.xxx:3306/xxx?useSSL=false"
              }
              """;
      DatabaseAdapter adapter = DatabaseAdapter.getAdapter();
      adapter.setConfig(config);
      AbstractJdbcDataSource dataSource = (AbstractJdbcDataSource) adapter.getDataSource();
      Pair<List<String>, List<List<String>>> chatModel = dataSource.getAllTableInfo();
      System.out.println(JSON.toJSONString(chatModel));
    }
}
```

## 后续计划
- 添加更多数据库类型支持（PG、Oracle）。
- 增加更多SQL操作功能，如查询表结构、查询表数据等。

## 注意事项

1. **配置格式**: 数据源配置需为合法的 JSON 格式。
2. **依赖版本**: 确保所有依赖版本与项目要求一致，避免兼容性问题。
3. **异常处理**: 在实际使用中，建议对配置加载和数据库连接过程中的异常进行捕获和处理。
