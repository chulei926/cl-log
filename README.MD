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

### 3.3 ProtoBuf

