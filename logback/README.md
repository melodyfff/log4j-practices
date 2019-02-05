# logback-practices

> 文档： https://logback.qos.ch/documentation.html  
> layouts参数：https://logback.qos.ch/manual/layouts.html
## 工作原理

![](doc/img/underTheHoodSequence2.gif)

## 最小依赖
```xml
    <!-- 自动导入 logback-classic logback-core slf4j-api -->
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>${logback.version}</version>
    </dependency>
```

## 与Spring

因为Logback直接实现了`SLF4J`，所以你只需要依赖两个库，即`jcl-over-slf4j`和`logback`）

```xml
      <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>${logback.version}</version>
      </dependency>

      <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>jcl-over-slf4j</artifactId>
        <version>${jcl-over-slf4j.version}</version>
      </dependency>
```