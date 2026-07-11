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


echo --- --- --- --- --- ---
echo Build docusaurus docs
echo --- --- --- --- --- ---


cd ../../website
npm run build
mkdir ../backend/src/main/resources/static/attack_vector_2
cp -r build/* ../backend/src/main/resources/static/attack_vector_2

cd ../frontend

echo --- --- ---
echo Build complete
echo.
echo Version created and installed in Backend
echo --- --- ---
