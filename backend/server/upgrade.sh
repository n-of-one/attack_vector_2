#!/bin/sh
cd ..
git pull
cd server
./build.sh
./run.sh
tail -f logs.txt
