
              _|_|      _|      _|                          _|            _|      _|                        _|
            _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|
            _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|
            _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|
            _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _|
    _|_|_|

A hacking simulation intended for use in Live Action RolePlaying games.


##Running:

The application is a Spring boot standalone java app, the main method class is: org.n1.mainframe.backend.AttackVector

Add environment variables for configuration:

`MONGODB_URI`     The connect URL for Mongo DB. For example: mongodb://attackvector:****@localhost/admin?authMechanism=SCRAM-SHA-1

`ENVIRONMENT`     The name of the server environment. For example: dev-ervi [1]

`MONGODB_NAME`    The name of the database. Optional, if not set, then the database from the MONGODB_URI is used. In this example: av2


[1] This is used, for example to simulate slower server-like response times in local development. (see StompService)


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







Deploy to Herouku:

> optional, if you want to do Heroku stuff (does not work in windows git-bash)
heroku login

> upload, build & deploy:
git push heroku master
