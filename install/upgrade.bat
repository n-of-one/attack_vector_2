@echo off
taskkill /F /IM java.exe
timeout /t 1 /nobreak
curl -L https://github.com/n-of-one/attack_vector_2/releases/latest/download/app.jar -o app.jar
call run.bat
