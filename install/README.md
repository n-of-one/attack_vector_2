# Installing Attack Vector

Log into the server (not as root) and do the following:

```bash
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
