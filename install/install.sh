#!/bin/bash
#
# Install script for Attack Vector 2
#
# Download install-scripts.zip from the latest release at:
# https://github.com/n-of-one/attack_vector_2/releases
#
# Extract the zip and run this script:
#
# unzip install-scripts.zip -d ~/av
# cd ~/av
# chmod +x install.sh
# ./install.sh
#

# install Mongodb, instructions from: https://www.mongodb.com/docs/manual/tutorial/install-mongodb-on-ubuntu/
curl -fsSL https://pgp.mongodb.com/server-7.0.asc | sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg --dearmor
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org

# reduce memory usage of MongoDB
sudo cp mongod.conf /etc/mongod.conf

sudo systemctl enable mongod
sudo systemctl start mongod

sleep 2

# Create MongoDB user
mongosh -file createUser.js

# Install Java
sudo apt install -y openjdk-21-jre-headless

# Make scripts executable
chmod +x setenv.sh
chmod +x run.sh
chmod +x upgrade.sh

sudo snap install --classic certbot
sudo ln -s /snap/bin/certbot /usr/bin/certbot

echo
echo
echo
echo
echo
echo "Installation complete. To run the application, execute the following command:"
echo
echo "./upgrade.sh"
echo
echo "or create the certificate using:"
echo "sudo certbot certonly --standalone -d attackvector.nl --register-unsafely-without-email"
echo
echo "sudo openssl pkcs12 -export -in /etc/letsencrypt/live/attackvector.nl/fullchain.pem -inkey /etc/letsencrypt/live/attackvector.nl/privkey.pem -out ~/keystore.p12 -name tomcat -CAfile /etc/letsencrypt/live/attackvector.nl/chain.pem -caname root"
echo
