
              _|_|      _|      _|                          _|            _|      _|                        _|
            _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|
            _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|
            _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|
            _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _|
    _|_|_|

A hacking simulation intended for use in Live Action RolePlaying games.

Version 2 release 0.1 (pre-alpha)


##Running:

The application is a Spring boot standalone java app, the main method class is: org.n1.mainframe.backend.AttackVector


##Create Mongodb user

Creating a user: av2 with password av2:

Step 1. log into a running mongo docker with:

  `docker exec -i -t mongo /bin/bash`

Step 2. start mongo client with

  `mongo`

Step 3. create user

  `use admin`

  `db.createUser( { user: "av2",
                   pwd: "av2",
                   roles: [ "readWrite"] },
                 { } )`


## Deploy to Herouku:

> optional, if you want to do Heroku stuff (does not work in windows git-bash)
heroku login

> upload, build & deploy:
git push heroku master


## Local development

You can set this environment variable:

`ENVIRONMENT`     The name of the server environment. For example: dev-ervi


This will be used to trigger specific development setting, for example to simulate slower server-like response times in local development. (see StompService)


## Heroku connecting to DB

Heroku makes use of environment variables for configuration. You can do so locally if you want to test this:

`MONGODB_URI`     The connect URL for Mongo DB. For example: mongodb://av2:av2@localhost/admin?authMechanism=SCRAM-SHA-1
`MONGODB_NAME`    The name of the database. Optional, if not set, then "av2" is used.

