rd /s /q build
call npm run build

cd build\static\js
del *.map
rename main* main.js

cd ..\..

del service-worker.js
del index.html
del manifest.json
del asset-manifest.json
del favicon.ico

mkdir js
move static\js\main.js js


rd /s /q ..\..\backend\src\main\resources\static
mkdir ..\..\backend\src\main\resources\static
xcopy /s * ..\..\backend\src\main\resources\static


cd ..

echo --- --- ---
echo Build complete
echo.
echo Version created and installed in Backend
echo --- --- ---
