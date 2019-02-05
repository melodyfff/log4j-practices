## Introduction


### Simple Logging Facade for Java (SLF4J)
`Simple Logging Facade for Java（SLF4J）`用作各种日志框架（例如java.util.logging，logback，log4j）的简单外观或抽象，允许最终用户在部署 时插入所需的日志记录框架。

#### 本地实现(Native implementations)

- [Logback](https://logback.qos.ch/)

#### 包装实现(Wrapped implementations)

- [JDK14](https://www.slf4j.org/api/org/slf4j/impl/JDK14LoggerAdapter.html)
- [Log4j](https://www.slf4j.org/api/org/slf4j/impl/Log4jLoggerAdapter.html)
- [Simple](https://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html)

![](doc/img/concrete-bindings.png)