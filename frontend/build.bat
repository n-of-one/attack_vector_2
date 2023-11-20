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


cd ..

echo --- --- ---
echo Build complete
echo.
echo Version created and installed in Backend
echo --- --- ---
