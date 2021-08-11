#!/bin/sh

SERVER_NAME="${project.artifactId}"         # 项目名称
JAR_NAME="${project.build.finalName}-${project.version}.jar"   # jar名称

cd `dirname $0`   # 进入bin目录cd
BIN_DIR=`pwd`     # bin目录绝对路径
cd ..             # 返回到上一级项目根目录路径
DEPLOY_DIR=`pwd`  # `pwd` 执行系统命令并获得结果

# 外部配置文件绝对目录,如果是目录需要/结尾，也可以直接指定文件
# 如果指定的是目录,spring则会读取目录中的所有配置文件
CONF_DIR=$DEPLOY_DIR/config

# 项目日志输出绝对路径
LOGS_DIR=$DEPLOY_DIR/logs
# 如果logs文件夹不存在,则创建文件夹
if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi
STDOUT_FILE=$LOGS_DIR/catalina.log

JAVA_MEM_OPTS="-server -Xms${service.mem} -Xmx${service.mem}"
CONFIG_FILES=" -Dlogging.path=$LOGS_DIR -Dspring.config.location=$CONF_DIR/ "
ACTIVE_PROFILE="-Dspring.profiles.active=${spring.profiles.active} "

echo "Starting the $SERVER_NAME ..."
nohup java $JAVA_OPTS $JAVA_MEM_OPTS $CONFIG_FILES $ACTIVE_PROFILE -jar $DEPLOY_DIR/lib/$JAR_NAME > $STDOUT_FILE 2>&1 &
mkdir -p /tmp/pid && echo $! > "/tmp/pid/${SERVER_NAME}.pid"
echo "OK!"
PIDS=`ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'`
echo "PID: $PIDS"
echo "STDOUT: $STDOUT_FILE"

