# Install script
#
# To run this script, first manually clone Attack Vector 2 repo to the server:
#
# git clone https://github.com/n-of-one/attack_vector_2.git
#
# Then run this script:
#
# cd attack_vector_2/backend/server/install
# chmod 770 install.sh
# ./install.sh
#

# install Mongodb, instructions from: https://www.mongodb.com/docs/manual/tutorial/install-mongodb-on-ubuntu/
curl -fsSL https://pgp.mongodb.com/server-7.0.asc | sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg --dearmor
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org
sudo systemctl start mongod
# sudo systemctl status mongod

# Create MongoDB user
mongosh -file createUser.js

# Install Java & Maven
sudo apt install -y openjdk-17-jre-headless
sudo apt install -y maven

# Port forwarding 80 -> 8080
sudo ufw allow 8080/tcp

sudo sed -i '1iCOMMIT' /etc/ufw/before.rules
sudo sed -i '1i-A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080' /etc/ufw/before.rules
sudo sed -i '1i:PREROUTING ACCEPT [0:0]' /etc/ufw/before.rules
sudo sed -i '1i*nat' /etc/ufw/before.rules

echo y | sudo ufw enable

# Make server scripts executable
cd ..
chmod 770 run.sh
chmod 770 setenv.sh
chmod 770 upgrade.sh

# Compile
cd ..
mvn package -DskipTests

# Run
cd server
./run.sh
