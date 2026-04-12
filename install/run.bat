@echo off
rem Source the setenv.sh script
call setenv.bat

rem Kill all java processes
taskkill /F /IM java.exe

rem Sleep for 1 second
timeout /t 1 /nobreak

rem Find the attackvector jar file
set JAR_FILE=
for %%f in (attackvector-*.jar) do set JAR_FILE=%%f
if "%JAR_FILE%"=="" (
    echo Error: No attackvector-*.jar file found in current directory
    exit /b 1
)

rem Start the Java application
java -jar %JAR_FILE%
