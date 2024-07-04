rm -rf build
npm run build

cd build/static/js
rm *.map

cd ../..

rm service-worker.js
rm manifest.json
rm asset-manifest.json
rm favicon.ico


rm -rf ../../backend/src/main/resources/static
mkdir ../../backend/src/main/resources/static
cp -r * ../../backend/src/main/resources/static


cd ..

echo --- --- ---
echo Build complete
echo.
echo Version created and installed in Backend
echo --- --- ---
