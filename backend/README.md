
              _|_|      _|      _|                          _|            _|      _|                        _|                                _|_|
            _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|      _|    _|
            _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|              _|
            _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|              _|
            _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _|            _|_|_|_|
    _|_|_|

A hacking simulation intended for use in Live Action RolePlaying games.

## Configuration

The application uses environment properties for basic configuration, all with defaults if you omit them. Implemented in `org.n1.av2.backend.config.ServerConfig`


`MONGODB_URI`     The connect URL for Mongo DB. Defaults to: mongodb://attackvector2:attackvector2@localhost/admin?authMechanism=SCRAM-SHA-1

`MONGODB_NAME`    The name of the database. Defaults to: attackvector2

`TIME_ZONE`     The IANA time zone, for instance: Europe/Amsterdam, defaults to system time zone.


Additional configuration can be performed when logged in as the 'admin' user.

## Running:

The application is a Spring boot standalone java app, the main function is: org.n1.av2.AttackVector

It can be started using maven: mvn clean install spring-boot:run


## Create Mongodb user
For local development/testing, create a user: attackvector2 with password attackvector2:

Step 1. start mongo client with

  `mongo`

Step 2. create user

  `use admin`

  `db.createUser( { user: "attackvector2",
                   pwd: "attackvector2",
                   roles: [ "readWrite"] },
                 { } )`
