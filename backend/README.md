
              _|_|      _|      _|                          _|            _|      _|                        _|
            _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|
            _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|
            _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|
            _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _|
    _|_|_|

A hacking simulation intended for use in Live Action RolePlaying games.

Version 2 release 0.1 (pre-alpha)

## Configuration

The application uses environment properties for configuration, all with defaults if you ommit them.

`MONGODB_URI`     The connect URL for Mongo DB. Defaults to: mongodb://av2:av2@localhost/admin?authMechanism=SCRAM-SHA-1

`MONGODB_NAME`    The name of the database. Defaults to: av2

<br />
The application uses the system default time zone, but this can be overriden using:

`TIME_ZONE`     The IANA time zone, for instance: Europe/Amsterdam

<br />
The application has an optional notion of differentiating between environments, such as local development, 
deployment on a local server during an event, or being hosted in the cloud. This is shown on certain screens,
and also used to tag database exports. Finally, this is also used to enable/disable some development features
such as simulating network delays when running the server locally. Development features
are enabled if the environment name starts with: dev. 

`ENVIRONMENT`     The name of the server environment. Defaults to: default 



## Running:

The application is a Spring boot standalone java app, the main method class is: org.n1.av2.backend

It can be started using maven: mvn clean install spring-boot:run


## Create Mongodb user

Creating a user: av2 with password av2:

Step 1. start mongo client with

  `mongo`

Step 2. create user

  `use admin`

  `db.createUser( { user: "av2",
                   pwd: "av2",
                   roles: [ "readWrite"] },
                 { } )`


