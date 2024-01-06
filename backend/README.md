
              _|_|      _|      _|                          _|            _|      _|                        _|                                _|_|
            _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|      _|    _|
            _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|              _|
            _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|              _|
            _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _|            _|_|_|_|
    _|_|_|

A hacking simulation intended for use in Live Action RolePlaying games.

Version 2 release 0.1 (pre-alpha)

## Configuration

The application uses environment properties for configuration, all with defaults if you omit them. Implemented in `org.n1.av2.backend.config.ServerConfig`


`MONGODB_URI`     The connect URL for Mongo DB. Defaults to: mongodb://attackvector2:attackvector2@localhost/admin?authMechanism=SCRAM-SHA-1

`MONGODB_NAME`    The name of the database. Defaults to: av2

`TIME_ZONE`     The IANA time zone, for instance: Europe/Amsterdam, defaults to system time zone.

`ENVIRONMENT`     The name of the server environment. Defaults to: default 

The application has an optional notion of differentiating between environments, such as local development,
deployment on a local server during an event, or being hosted in the cloud. This will be used to tag database exports. 
 
If the environment name starts with: `dev` then a small network delay is simulated, to give a more realistic 
experience when running the server locally when running the server locally.

If the environment name starts with: `dev` then the `quickscan` & `qs` and `quickattack` & `qa` commands are enabled.


`GOOGLE_CLIENT_ID`   When using Google login, set this property. Format: 012345678901-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com  . Defaults to: none

`FRONTIER_ORTHANK_TOKEN`   Security token to access Orthank (Frontier Larp only). Defaults to: none 

## Running:

The application is a Spring boot standalone java app, the main method class is: org.n1.av2.backend

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
