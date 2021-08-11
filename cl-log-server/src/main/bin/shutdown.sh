#!/bin/sh
SERVER_NAME="${project.artifactId}"

kill -15 $(cat /tmp/pid/${SERVER_NAME}.pid)
rm -rf /tmp/pid/${SERVER_NAME}.pid >/dev/null 2>&1

echo "${SERVER_NAME} stopped"
