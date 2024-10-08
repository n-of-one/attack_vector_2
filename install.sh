# Install script
#
# To run this script, first manually clone Attack Vector 2 repo to the server:
#
# git clone https://github.com/n-of-one/attack_vector_2.git
#
# Then run this script:
#
# cd attack_vector_2
# chmod 770 install.sh
# ./install.sh
#

# install Mongodb, instructions from: https://www.mongodb.com/docs/manual/tutorial/install-mongodb-on-ubuntu/
curl -fsSL https://pgp.mongodb.com/server-7.0.asc | sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg --dearmor
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org

# reduce memory usage of MongoDB
sudo cp install/mongod.conf /etc/mongod.confls

sudo systemctl enable mongod
sudo systemctl start mongod

sleep 2
# sudo systemctl status mongod

# Create MongoDB user
mongosh -file install/createUser.js

# Install Java & Maven
sudo apt install -y openjdk-21-jre-headless
sudo apt install -y maven

# Allow Java to bind to port 80
sudo setcap CAP_NET_BIND_SERVICE=+eip /usr/lib/jvm/java-21-openjdk-arm64/bin/java

# prepare regular scripts for running the application
cp install/setenv.sh ~
cp install/run.sh ~
cp install/upgrade.sh ~
cd ..
chmod 770 setenv.sh
chmod 770 run.sh
chmod 770 upgrade.sh


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
echo "sudo openssl pkcs12 -export -in /etc/letsencrypt/live/attackvector.nl/fullchain.pem -inkey /etc/letsencrypt/live/attackvector.nl/privkey.pem -out ~ubuntu/keystore.p12 -name tomcat -CAfile /etc/letsencrypt/live/attackvector.nl/chain.pem -caname root"
echo
echo "sudo chown ubuntu:ubuntu ~ubuntu/keystore.p12"

