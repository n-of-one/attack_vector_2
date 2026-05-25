#!/bin/bash

# Building and exporting docker files to run on another device (ie. a Raspberry Pi 3)
mkdir export

echo 'Copy env and conf'
cp ./.env ./export
cp ./letsencrypt.env ./export
cp -R ./conf/ ./export
mkdir ./export/conf/mongoDB
cp -R ../../install/createUser.js ./export/conf/mongoDB

echo 'Clean cache and build a new docker image of AV2'
echo 'This step can be long and take more than 10 minutes'
docker build --no-cache ../../. -t av2:latest --platform linux/arm64

echo 'Export the docker image as tar file'
docker save -o ./export/av2image.tar av2:latest

echo 'Copy and modify the compose.yaml file to run outside of the repo'
cp ./compose.yaml ./export
cp ../../compose.yaml ./export/av2only_compose.yaml

line_old1='file: ../../compose.yaml'
line_new1='file: ./av2only_compose.yaml'
sed -i "s%$line_old1%$line_new1%g" ./export/compose.yaml

sed -i "s/build:/image: av2:latest/g" ./export/av2only_compose.yaml
sed -i "s/context: .//g" ./export/av2only_compose.yaml

line_old2='./install/createUser.js'
line_new2='./conf/mongoDB/createUser.js'
sed -i "s%$line_old2%$line_new2%g" ./export/av2only_compose.yaml

echo 'Creating a run.sh file'
echo "#!/bin/bash

chmod 600 ./conf/traefik/acme.json
docker image inspect av2 > /dev/null
RET=\$?
if [[ \${RET} -eq 1 ]]
then
   echo 'Loading AV2 docker image'
   docker load -i av2image.tar
fi
docker compose up" > ./export/run.sh

echo 'Creating a run.sh file'
echo "#!/bin/bash

echo 'Loading AV2 docker image'
docker load -i av2image.tar
docker compose up" > ./export/update.sh