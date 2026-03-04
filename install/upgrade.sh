#!/bin/sh
killall java
sleep 1
curl -L https://github.com/n-of-one/attack_vector_2/releases/latest/download/app.jar -o app.jar
./run.sh
