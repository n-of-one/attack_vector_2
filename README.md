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
such as an AWS EC2 instance. It runs on a t4g.micro with a total operating cost (at the time of writing) of less than 15 euro per month.

It's open source software with the MIT license which means you can use it for commercial purposes.

## Technical stuff

The repo consists of two separate projects:

- frontend: browers-UI (React/Redux)
- backend: server (Spring boot/Kotlin)

As well as stuff like the installation files.

## Installation

### Linux
See [install/README-linux.md](install/README-linux.md)


### Windows
See [install/README-windows.md](install/README-windows.md)

Note: the instructions for Windows will start it up in development mode, allowing login as any user without password.
This is not recommended for use in a Larp.


## Running Attack Vector
The commands to manage the server are in the folder that you started in. You can start the server with: `./run.sh` or `./run.bat`

You can upgrade to the latest version from Github with `./upgrade.sh` or `./upgrade.bat`


## Development instructions

Clone this repo and then create two separate Intellij projects. Once for frontend and one for backend. Each contains a separate README.md with more instructions for that
part.

Requires separate Mongodb.

## Creating a release
AV2 uses trunk based development. The main branch is the latest stable version.

When making changes: 
- In Frontend: run the release.bat script. This will build the React application and copy all relevant files to the backend directory.
- Commit & push all changes.

