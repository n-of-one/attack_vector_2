#!/bin/sh
killall java
sleep 1
rm -f attackvector-*.jar
JAR_URL=$(curl -s https://api.github.com/repos/n-of-one/attack_vector_2/releases/latest | grep "browser_download_url" | grep "\.jar" | head -1 | sed 's/.*"browser_download_url": *"//' | sed 's/".*//')
JAR_NAME=$(basename "$JAR_URL")
echo "Downloading: $JAR_NAME"
curl -L "$JAR_URL" -o "$JAR_NAME"
./run.sh
