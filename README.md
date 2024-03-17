             _|_|      _|      _|                          _|            _|      _|                        _|
           _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|
           _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|
           _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|
           _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _| 
    _|_|_|

*A hacking simulation intended for use in Live Action RolePlaying games.*

## More info

The website for Attack Vector 2 is located here: https://attackvector.nl/website

## Version 2

This is the successor of Attack Vector (v1). 

Besides general improvements and a visual makeover, version 2 has the following new features

- Realtime multiplayer: hack the same ICE with multiple hackers at the same time
- Rudimentary integration with offline items like door-lights

## Using Attack Vector 2 at your LARP event

The goal is to make it as easy as possible to run AV at your larp event. There are scripts to install and upgrade the software on a fresh Ubuntu server,
such as an AWS EC2 instance. It runs on a t4g.micro with a total operating cose (at the time of writing) of less than 15 euro per month.

It's open source software with the MIT license which means you can use it for commercial purposes.

## Technical stuff

The repo consists of two separate projects:

- frontend: browers-UI (React/Redux)
- backend: server (Spring boot/Kotlin)

As well as stuff like the installation files.

## Installation instructions
These instructions assume you have a fresh Ubuntu server available for AV2, and are logged in as a user with sudo rights. This is the default setup for an EC2 Ubuntu instance.


First clone the github repo:

`git clone https://github.com/n-of-one/attack_vector_2.git`


Then run this script:

```
cd attack_vector_2
chmod 770 install.sh
./install.sh
```

This will:
- Download and install MongoDB
- Download and install Java 21
- Download and install Certbot (for a https certificate)
- Create a MongoDB user with a default password. This is only ok if you don't have any other users on this server.
- Set up the scripts to start and upgrade AV2 in the root folder.
- Set up Ubuntu to allow AV2 to run on ports 80 and 443
- Give instructions on how to set up a letsencrypt https certificate.


The commands to manage the server are in the folder that you started in. You can start the server with: `./run.sh`

You can edit the setenv.sh file to customize the installation to your needs: `vi setenv.sh`  See [README.md](/backend/README.md) file for details.


You can upgrade to the latest version from Github with `./upgrade.sh`


If you want run AV2 on port 443 with https, then edit this file: [application.properties]( backend/src/main/resources/application.properties) and uncomment
the last part of the file. It's located on your server in the folder: `attack_vector_2/backend/src/main/resources/application.properties`.


## Development instructions

Clone this repo and then create two separate Intellij projects. Once for frontend and one for backend. Each contains a separate README.md with more instructions for that
part.

Requires separate Mongodb (for instance in a docker).

## Creating a release
AV2 uses trunk based development. The main branch is the latest stable version.

When making changes: 
- In Frontend: run the release.bat script. This will build the React application and copy all relevant files to the backend directory.
- Commit & push all changes.

