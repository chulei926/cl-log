@echo off
title ${project.build.finalName}

SET CURRENT_DIR=%~dp0

SET DEPLOY_DIR=%CURRENT_DIR%..
SET CONF_DIR=%DEPLOY_DIR%\config
SET LOGS_DIR=%DEPLOY_DIR%\logs

SET JAR_NAME="${project.build.finalName}-${project.version}.jar"

java -Dfile.encoding=GBK -server -Xms${service.mem} -Xmx${service.mem} -Dlogging.path=%LOGS_DIR% -Dspring.config.location=%CONF_DIR%\ -Dspring.profiles.active=${spring.profiles.active}  -jar %DEPLOY_DIR%/lib/%JAR_NAME%
pause
