# Installing Attack Vector on Linux

These instructions assume you have a fresh Ubuntu server available and are logged in as a user with sudo rights. This is the default setup for an EC2 Ubuntu instance.


Log into the server (not as root) and do the following:

```bash
# Optionally set the time zone if needed
sudo timedatectl set-timezone Europe/Amsterdam

git clone https://github.com/n-of-one/attack_vector_2.git

cd attack_vector_2
chmod 770 install.sh
./install.sh
```


# Creating a certificate:

```bash
sudo snap install --classic certbot
sudo ln -s /snap/bin/certbot /usr/bin/certbot
sudo certbot certonly --standalone -d attackvector.nl --register-unsafely-without-email

sudo openssl pkcs12 -export -in /etc/letsencrypt/live/attackvector.nl/fullchain.pem -inkey /etc/letsencrypt/live/attackvector.nl/privkey.pem -out ~ubuntu/keystore.p12  -name tomcat -CAfile /etc/letsencrypt/live/attackvector.nl/chain.pem -caname root
```
