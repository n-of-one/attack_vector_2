             _|_|      _|      _|                          _|            _|      _|                        _|
           _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|
           _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|
           _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|
           _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _| 
    _|_|_|

*A hacking simulation intended for use in Live Action RolePlaying games.*

## More info

The website for Attack Vector 2 is located here: https://n-of-one.github.io/attack_vector_2/docs/

## Version 2

This is the successor of Attack Vector (v1). 

Besides general improvements and a visual makeover, version 2 has the following new features

- Realtime multiplayer: hack the same ICE with multiple hackers at the same time
- Rudimentary integration with offline items like door-lights

## Release notes

See the [release notes](RELEASE_NOTES.md) for the latest changes.


## Using Attack Vector 2 at your LARP event

The goal is to make it as easy as possible to run AV at your larp event. There are scripts to install and upgrade the software on a Windows machine
and on an Ubuntu server, such as an AWS EC2 instance. It runs on a t4g.micro with a total operating cost (at the time of writing) of less than 15 euro per month.

It's open source software with the MIT license which means you can use it for commercial purposes.

## Installation

Download `install-scripts.zip` from the latest release at:
https://github.com/n-of-one/attack_vector_2/releases

### Linux
See [install/README-linux.md](install/README-linux.md)

### Windows
See [install/README-windows.md](install/README-windows.md)

Note: the instructions for Windows will start it up in development mode, allowing login as any user without password.
This is not recommended for use in a Larp.


## Running Attack Vector
Start the server with: `./run.sh` or `run.bat`

Update to the latest version with `./upgrade.sh` or `upgrade.bat`

Attack Vector runs on port 80 (windows) or port 443 (linux) by default.


## First time login as admin

To log in as Admin, click on the 2 in the big "Attack Vector 2" banner on the login screen. This will take you to the admin login page.
Alternatively you can navigate to "/adminLogin" in the browser. This will open up a login page where you can enter username and password.
Enter username "admin" and click "sign in".

The first thing you want to do is set a master password. Find the configuration item "Password" and enter a secure password
that you don't use for anything else.

## Development

The repo consists of four separate projects:

- frontend: browser-UI (React/Redux)
- backend: server (Spring boot/Kotlin)
- website: website (Docusaurus)
- tests: e2e tests (Playwright)

Clone this repo and then create two separate Intellij projects. One for frontend and one for backend. Each contains a separate README.md with more instructions for that
part.

Requires separate Mongodb.

## Creating a release
AV2 uses trunk based development. The main branch is the latest stable version.

When making changes:
- In Frontend: run the release.bat script. This will build the React application and copy all relevant files to the backend directory.
- Commit & push all changes.
- Tag the commit with a version tag (e.g. `v2.19.1`) and push the tag. This triggers a GitHub Actions workflow that builds the jar and creates a release.

