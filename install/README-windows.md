# Installing Attack Vector on Windows

These instructions assume you have a Windows machine and are logged in as a user with admin rights.
Attack Vector 2 does not strictly require admin rights, but this makes the installation process simpler
because you can install software to `C:\Program Files`.


## 1. GIT 
Download & Install GIT from: https://git-scm.com/downloads
- Download for windows
- Install, use all the default options


## 2. Attack Vector code
Open a command prompt and run:

`cd \ `

`mkdir av`

`cd \av`

`git clone https://github.com/n-of-one/attack_vector_2.git`

This should download the sources and create an `attack_vector2` directory.

`cd attack_vector_2`

`install.bat`

This will copy the files for running AV to the folder `C:\av`


## 2. Java

2.1 Download and install JDK 21. Google: `download jdk 21 windows` to find the latest place where you can download the JDK 21. At the time of writing 
it was here: https://www.oracle.com/nl/java/technologies/downloads/#jdk21-windows
Download and run the installer. You need the JDK, not the JRE.

2.2 Check in `C:\Program Files\Java` to see the exact path where java was installed. Should be something like:
   `C:\Program Files\Java\jdk-21`

Edit the file `C:\av\setenv.bat` . Fix line 2 so that it points to your Java location

2.3 Check that everything works by running in the command prompt:

`setenv`

`"%JAVA_HOME%\bin\java" -version`

You should see some text about the java version


## 3. Maven
We are going to build the latest version of AttackVector using Maven. Google `download maven 3` and download the
"Binary zip archive"

Unzip the downloaded zip file in the `C:\av` so that you get a `C:\av\maven-3.x.y` folder.

Check the `C:\av` folder for the exact maven path, which should be something like: `C:\AV\apache-maven-3.9.8`

Edit the file `C:\av\setenv.bat` . Fix line 3 so that it points to your maven location

In the command prompt run:

`%MVN_HOME%\bin\mvn -version`

You should see some text about the maven version


## 4. MongoDB
Attack Vector is tested to work on windows with version 7.0.7, but will probably work with higher versions without any
problems.

4.1 Download MongoDB from https://www.mongodb.com/download-center/community
- Install with default values
- Choose: 'Complete' when asked.
- Uncheck the box to "Install MongoDB as a Service" 
- Installing MongoDB Compass is optional, it allows you to view the database if you want that. It's not necessary.

4.2 Create the directories: `C:\data` and `C:\data\db`. This is where the database will be stored.

4.3 Edit the file `C:\av\_attack_vector\mongod.bat` . Check that the path in this file is correct.

4.3 Open a second new(!) command prompt and run:

`cd \av\`

`mkdir db`

Star the database with:

`mongod`

If this command terminates (you can type things again in the command prompt) then MongoDB failed to start.
If there is a lot of text, but you cannot type anything, then MongoDB is running. Leave this command prompt open while 
running attack vector.

4.4 Downlaod MongoDB shell from https://www.mongodb.com/try/download/shell
- As the installation path, choose: `C:\av\mongosh\`

4.5 In the first command prompt (not running the database) run:

`cd \av\mongosh\`

`mongosh`

you should now see something like: "test>" and should be able to enter commands to the database.

We are going to create a user to allow Attack Vector to access the database. Run: 

`use admin`

you should see: "switched to db admin"

`db.createUser( { user: "attackvector2", pwd: "attackvector2", roles: [ "readWrite"] }, { } )`

you should see: "{ ok: 1 }"

`exit`

To exit the shell.

## 5. First time running

In the command prompt 

`cd \av`

`upgrade`

This should start building AttackVector on your system. The first time this will take very long, and download a lot of
files. It is ok for this to take 10 minutes. Subsequently, this will be much faster.

At the end it should start up attack vector and you should see a message like: "Started AttackVectorKt in 5.622 seconds
(process running for 6.354)"

(If the command prompt terminates, then something went wrong.)

If it was successful, open a browser and go to: "http://localhost" you should see the login screen.


>There might be a popup from Windows Defender Firewall. Allow access to all networks.


> **Troubleshooting**
> 
> The database needs to be running before you start Attack Vector. If you see an error containing the
> text "Error creating bean with name 'DbSchemaVersioning"  then the database is probably not running.



## 6. Running Attack Vector
Once you have completed the above steps, this is how you start attack vector:

- Close any command prompts that are open from previous steps. (This will stop the database and the server
- Open a new command prompt and run: `cd \av` and then `mongod`) leaven this tab open
- Open a new command prompt and run: `cd \av` and then `run`) leaven this tab open


## 7. Updating Attack Vector
If new features or a bug fix is released, you can update your version by doing the following:

- Close the command prompt running Attack Vector
- Open a new command prompt and run: `cd \av` and then `upgrade` . This will download the latest version from github, 
build it and start it. 
