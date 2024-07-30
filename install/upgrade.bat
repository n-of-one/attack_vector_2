@echo off
taskkill /F /IM java.exe
call setenv.bat
cd attack_vector_2
git stash
git pull
git stash apply
git stash clear
cd backend
call %MVN_HOME%\bin\mvn clean install -DskipTests
cd ..\..
call run.bat