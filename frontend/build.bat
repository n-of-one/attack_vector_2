echo off
echo --- --- --- ---
echo Build frontend
echo --- --- --- ---

rd /s /q build
call npm run build

cd build\static\js
del *.map

cd ..\..

del service-worker.js
del manifest.json
del asset-manifest.json
del favicon.ico


rd /s /q ..\..\backend\src\main\resources\static
mkdir ..\..\backend\src\main\resources\static
xcopy /s * ..\..\backend\src\main\resources\static

echo --- --- --- --- --- ---
echo Build docusaurus docs
echo --- --- --- --- --- ---

cd ..\..\website
call npm run build
mkdir ..\backend\src\main\resources\static\attack_vector_2
xcopy /s build ..\backend\src\main\resources\static\attack_vector_2

cd ..\frontend

echo --- --- --- --- --- --- --- --- --- --- ---
echo Build complete
echo.
echo Version created and installed in Backend
echo --- --- --- --- --- --- --- --- --- --- ---
