#!/bin/sh
killall java
cd attack_vector_2
git stash
git pull
git stash apply
git stash clear
cd backend
mvn clean install -DskipTests
cd ../..
./run.sh
