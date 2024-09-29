
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

Also, if the environment name starts with: `dev`:
- a small network delay is simulated, to give a more realistic experience when running the server locally when running the server locally.
- the `quickscan` & `qs` and `quickattack` & `qa` commands are enabled.
- no admin password is needed


`GOOGLE_CLIENT_ID`   When using Google login, set this property. Format: 012345678901-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com  .
Defaults to: none  .

To create this: set up a project in the Google cloud console, and create oauth client credentials for a web application .
(https://console.cloud.google.com/apis/credentials). Requires https URLs unless it's for localhost.

`FRONTIER_ORTHANK_TOKEN`   Security token to access Orthank (Frontier Larp only). Defaults to: none

`ADMIN_PASSWORD`   Password for Admin logins. Defaults to: disabled (with this value, admin logins are disabled)

The application allows admins to log in with username and password. There is only one password for the entire site. Please use a strong password for this,
for example a 32 character random string.

`LOCAL_CONTENT_FOLDER`  The folder where the content is stored. Defaults to: local

If you want to locally host content that will be available for hackers to discover, you can create this folder in the folder where your attack vector
startup script resided. Then you can access the files of that folder via the path: /local/{filename} . For example: for the site attackvector.nl if
you create a folder local with a file info.txt then you can access it via the URL: https://attackvector.nl/local/info.txt . You can also create subdirectories if you want.
The purpose of local files is that you can link to them from text-layers. This way the hackers can discover the files when hacking these layers.

Note that there is no protection on these files, so anyone who can guess the file name can access these files. Do not put off-game sensitive information here!
For example: do not make your attack_vector installation directory the local folder, because then anyone can access the setenv.sh script that contains your ADMIN_PASSWORD.


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
