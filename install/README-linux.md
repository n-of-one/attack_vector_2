# Installing Attack Vector on Linux

These instructions assume you have a fresh Ubuntu server available and are logged in as a user with sudo rights. This is the default setup for an EC2 Ubuntu instance.


## 1. Download and install

Log into the server (not as root) and do the following:

```bash
# Optionally set the time zone if needed
sudo timedatectl set-timezone Europe/Amsterdam
```

Download `install-scripts.zip` from the latest release at:
https://github.com/n-of-one/attack_vector_2/releases

```bash
cd ~
curl -LO https://github.com/n-of-one/attack_vector_2/releases/latest/download/install-scripts.zip
unzip install-scripts.zip -d ~/av
cd ~/av
chmod +x install.sh
./install.sh
```

This installs MongoDB, Java, and sets up the scripts. After installation, download and start Attack Vector with:

```bash
./upgrade.sh
```

## 2. Running Attack Vector

Once installed, start Attack Vector with:

```bash
cd ~/av
./run.sh
```

To update to the latest version:

```bash
cd ~/av
./upgrade.sh
```

## 3. Creating a certificate (optional)

```bash
sudo snap install --classic certbot
sudo ln -s /snap/bin/certbot /usr/bin/certbot
sudo certbot certonly --standalone -d attackvector.nl --register-unsafely-without-email

sudo openssl pkcs12 -export -in /etc/letsencrypt/live/attackvector.nl/fullchain.pem -inkey /etc/letsencrypt/live/attackvector.nl/privkey.pem -out ~/av/keystore.p12  -name tomcat -CAfile /etc/letsencrypt/live/attackvector.nl/chain.pem -caname root
```
