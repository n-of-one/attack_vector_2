             _|_|      _|      _|                          _|            _|      _|                        _|
           _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|
           _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|
           _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|
           _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _| 
    _|_|_|

*A hacking simulation intended for use in Live Action RolePlaying games.*

## Version 2

This is the successor of Attack Vector (v1). See here for more information on the previous verion: http://adventureforum.nl/ld50/attack_vector.html

Besides general improvements and a visual makeover, version 2 has the following new features

- Realtime multiplayer (hack the same ICE with multiple hackers at the same time)
- Rudimentary integration with offline items like doors-lights

## Using Attack Vector 2 at your LARP

The goal is to run AV2 in the cloud, managed by me (Erik) so there will be no hassle to install software for the larp organizers.
It looks like running AV2 on AWS will cost less than 2 euro per day, so for now I'll be offering this for free for any larp
that wants to use it for the duration of the event.

It's open source software, so you can always choose to run it yourself.

The license is MIT which means you can use it for commercial purposes.

## Technical stuff

The repo consists of two separate projects:

- frontend: browers-UI (React/Redux)
- backend: server (Spring boot/Kotlin)

As well as stuff like the Gitlab CI/CD pipeline, Dockerfiles, etc.

## Development instructions

Clone this repo and then create two separate Intellij projects. Once for frontend and one for backend. Each contains a separate README.md with more instructions for that
part.

Requires separate Mongodb (for instance in a docker).

## Creating a release

- In Frontend: run the release.bat script. This will build the Redux application and copy all relevant files to the backend directory.
- Commit & push changes to backend.
- (Optionally) package the backend. If you deploy to Heroku, you don't need this.

