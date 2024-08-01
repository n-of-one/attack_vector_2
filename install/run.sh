#!/bin/bash
. setenv.sh
killall java
sleep 1

#re-allow java to run on port 80, in case java was upgraded and the permissions were lost
sudo setcap CAP_NET_BIND_SERVICE=+eip /usr/lib/jvm/java-21-openjdk-arm64/bin/java

nohup java -jar attack_vector_2/backend/target/app.jar --server.port=443 --server.ssl.key-store=file:/home/ubuntu/keystore.p12 --server.ssl.key-store-password=password --server.ssl.key-store-type=pkcs12 --server.ssl.key-alias=tomcat --server.ssl.key-password=password >>logs.txt 2>>errors.txt &
#nohup java -jar attack_vector_2/backend/target/app.jar --server.port=80 >>logs.txt 2>>errors.txt &

tail -f logs.txt
