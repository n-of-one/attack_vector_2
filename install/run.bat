@echo off
rem Source the setenv.sh script
call setenv.bat

rem Kill all java processes
taskkill /F /IM java.exe

rem Sleep for 1 second
timeout /t 1 /nobreak

rem Start the Java application
java -jar app.jar
