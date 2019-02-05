## log4j-practices

> 文档：https://logging.apache.org/log4j/2.x/

### 最小依赖

引入`log4j-core`后会自动引入 `log4j-api`

```xml
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-core</artifactId>
      <version>${log4j.version}</version>
    </dependency>
```

### 与Springframework

Spring中的强制日志记录依赖是`Jakarta Commons Logging API（JCL）`

要使用`Log4j 2.X`与`JCL`，所有你需要做的就是把`Log4j`的在类路径，并为其提供一个配置文件(`log4j2.xml`，`log4j2.properties`或其他 支持的配置格式)

#### 最小依赖

```xml
      <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>${log4j.version}</version>
      </dependency>

      <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-jcl</artifactId>
        <version>${log4j.version}</version>
      </dependency>
```

如果希望将`SLF4J`委派给`Log4j`，例如对于默认情况下使用SLF4J的其他库
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    private static final Logger log = LoggerFactory.getLogger(Test.class);
}
```

还需添加以下依赖

```xml
      <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>${slf4j-api.version}</version>
      </dependency>
      
      <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>${log4j.version}</version>
      </dependency>
```

### 避免使用Commons Logging
- 排除`spring-core`模块的依赖关系（因为它是唯一明确依赖的模块`commons-logging`）
- 依赖于一个特殊的`commons-logging`依赖项，用一个空`jar`替换库（更多细节可以在[SLF4J FAQ](http://slf4j.org/faq.html#excludingJCL)中找到 ）

```xml
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-core</artifactId>
      <exclusions>
        <exclusion>
          <groupId>commons-logging</groupId>
          <artifactId>commons-logging</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
```