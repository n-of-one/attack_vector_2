#!/bin/sh
killall java
cd attack_vector_2
git pull
cd backend
mvn clean install -DskipTests
cd ../..
./run.sh
