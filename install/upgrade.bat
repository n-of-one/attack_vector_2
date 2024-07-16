@echo off
call setenv.bat
cd attack_vector_2
git pull
cd backend
call %MVN_HOME%\bin\mvn clean install -DskipTests
cd ..\..
call run.bat