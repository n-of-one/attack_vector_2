@echo off
taskkill /F /IM java.exe
timeout /t 1 /nobreak
del attackvector-*.jar 2>nul
for /f "tokens=*" %%a in ('powershell -NoProfile -Command "$r = Invoke-RestMethod 'https://api.github.com/repos/n-of-one/attack_vector_2/releases/latest'; ($r.assets | Where-Object { $_.name -like '*.jar' } | Select-Object -First 1).name"') do set JAR_NAME=%%a
echo Downloading: %JAR_NAME%
curl -L "https://github.com/n-of-one/attack_vector_2/releases/latest/download/%JAR_NAME%" -o "%JAR_NAME%"
call run.bat
