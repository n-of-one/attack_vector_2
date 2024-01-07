#!/bin/bash
. setenv.sh
killall java
sleep 1
nohup java -jar sources/backend/target/app.jar --server.port=8080 >>logs.txt 2>>errors.txt &
#nohup java -jar sources/backend/target/app.jar --server.port=8080 -Xmx512m -XX:MaxMetaspaceSize=256m >> logs.txt 2>> errors.txt &
