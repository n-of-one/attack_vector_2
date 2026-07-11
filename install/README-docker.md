# Installing Attack Vector on any platform with docker

# Install docker

Install docker by following the instruction from docker official website :
* Windows --> https://docs.docker.com/desktop/setup/install/windows-install/
* Linux --> https://docs.docker.com/desktop/setup/install/linux/
* Mac --> https://docs.docker.com/desktop/setup/install/mac-install/

# Start containers
```bash
git clone https://github.com/n-of-one/attack_vector_2.git

cd attack_vector_2
docker compose up -d
```

# Stop containers
```bash
cd attack_vector_2
docker compose down
```