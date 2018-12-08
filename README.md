
             _|_|      _|      _|                          _|            _|      _|                        _|
           _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|
           _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|
           _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|
           _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _| 
    _|_|_|

*A hacking simulation intended for use in Live Action RolePlaying games.*

More info: http://adventureforum.nl/ld50/attack_vector.html ( screenshot and links to explanation videos).

##Version

This is version 2 which consists of

- frontend: browers-UI (React/Redux)
- backend: server (Spring boot/Kotlin)

This branch is work in progress. The stable branch is "v1".


##Development instructions:
Clone this repo and then create two separate Intellij projects. Once for frontend and one for backend. Each contains a separate README.md with more instructions for that part.

Requires separate Mongodb (for instance in a docker).

##Creating a release
- In Frontend: run the release.bat script. This will build the Redux application and copy all relevant files to the backend directory.
- Commit & push changes to backend.
- (Optionally) package the backend. If you deploy to Heroku, you don't need this.

