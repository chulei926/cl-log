# 自研日志收集系统

## 1. 架构

## 2. 模块

### 2.1 cl-log-config
    config模块，提供一些公共的配置信息。
    
### 2.1 cl-log-web
    web模块，用于配置一些信息以及对 agent 和 server 的监控。
    
### 2.1 cl-log-agent
    agent模块，用于收集指定的日志。
    
### 2.1 cl-log-server
    server模块，主要作用是将agent收集的日志转化成标准结构，写入到指定的数据库。

## 3. 相关技术

### 3.1 netty

### 3.2 zookeeper

### 3.3 Google ProtoBuf

#### 3.3.1 介绍  

> Protocol buffers are Google's language-neutral, platform-neutral, extensible mechanism for serializing structured data – think XML, but smaller, faster, and simpler. You define how you want your data to be structured once, then you can use special generated source code to easily write and read your structured data to and from a variety of data streams and using a variety of languages.

> Protocol buffers是Google公司的**与语言无关、平台无关、可扩展的序列化数据的机制**，类似XML，但是更小、更快、更简单。
> 您只需定义一次数据的结构化方式，然后就可以使用特殊生成的源代码，轻松地**将结构化数据写入和读取到各种数据流中**，并**支持多种语言**。

#### 3.3.2 使用  

* 下载编译器 protoc-3.12.2-win64.zip 。

* 添加maven依赖

```xml
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.6.1</version>
</dependency>
```

* 编写 proto 文件 Log.proto
```protobuf
syntax = "proto3";
option optimize_for = SPEED;
option java_package = "com.cl.log.config.model";
option java_outer_classname = "LogFactory";

message Log{

  enum Category{
    nginx_log = 0;
    tomcat_access_log = 1;
    biz_log = 2;
    perf_log = 3;
  }

  Category category = 1;

  oneof body{
    NginxLog nginxLog = 2;
    TomcatAccessLog tomcatAccessLog = 3;
    BizLog bizLog = 4;
    PerfLog perfLog = 5;
  }
}

message NginxLog{
  string host = 1; // 主机
  string dateTime = 2;  // 时间
  string requestMethod = 3;
  string requestURL = 4;
  string params = 5;
  string statesCode = 6;  // 状态码
  int32 consume = 7; // 耗时
  string userAgent = 8; //
  string ip = 9; // ip
  string upstream = 10;
}

message TomcatAccessLog{
  string host = 1; // 主机
  string traceId = 2; // 调用链id
  string biz = 3; // 业务组件
  int32 port = 4; // 端口
  string userId = 5;  // 用户ID
  string dateTime = 6; // 时间
  string statesCode = 7;  // 状态码
  int32 consume = 8; // 耗时
  string ip = 9; // ip
  string requestMethod = 10;
  string requestURL = 11;
  string params = 12;
}

message BizLog{
  string host = 1; // 主机
  string biz = 2; // 业务组件
  string traceId = 3; // 调用链id
  string dateTime = 4; // 日期时间
  string thread = 5;  // 线程
  string level = 6; // 日志级别
  string clazz = 7; // 类名
  string msg = 8; // 消息
}

message PerfLog{
  string host = 1; // 主机
  string biz = 2; // 业务组件
  string clazz = 3; // 接口全类名
  string method = 4; // 方法
  string consume = 5; // 耗时
  string dateTime = 6; // 日期时间
}
```

* 使用编译器，通过.proto文件生成代码
```shell script
protoc --java_out=./ ./Log.proto
```

* 在发送端添加编码器，在接收端添加解码器
```java
// 发送端（客户端）
pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
pipeline.addLast("encoder", new ProtobufEncoder());

// 接收端（服务端）
pipeline.addLast(new ProtobufVarint32FrameDecoder());
pipeline.addLast("decoder", new ProtobufDecoder(LogFactory.Log.getDefaultInstance()));
```

#### 3.3.3 分析Protocol的粘包、拆包

参考：https://juejin.im/post/6847902220768411656

实际上直接使用Protocol编解码器还是存在粘包问题的。
常见如下异常：
> com.google.protobuf.InvalidProtocolBufferException: While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either that the input has been truncated or that an embedded message misreported its own length.

意思是：分析protocol消息时，输入意外地在字段中间结束。这可能意味着输入被截断，或者嵌入的消息误报了自己的长度。

其实就是粘包问题，多条数据合并成一条数据了，导致解析出现异常。

只需要在发送端加上编码器```ProtobufVarint32LengthFieldPrepender```

接收方加上解码器```ProtobufVarint32FrameDecoder```



