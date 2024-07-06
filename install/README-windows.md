Notes on how to install AttackVector on windows.

---------------------
0. Starting from scratch by insalling Git
   https://git-scm.com/downloads
   0.1 Download for windows.
   0.2 Install, use all the default options

Then choose a folder that will be the root of your installation. Let's assume that it is:

c:\AV

0.3 create that directory in the Windows explorer.
0.4 open a command prompt and run:

`cd \AV`

`git clone https://github.com/n-of-one/attack_vector_2.git`

This should download the sources and create a _attack_vector directory.

`cd attack_vector_2`

`install`

This will copy the files for running AV to the folder C:\av

0.5 (Close this command prompt)

---------------------
1. Java (jdk 21)

Google: download jdk 21 windows
(For example: https://www.oracle.com/nl/java/technologies/downloads/#jdk21-windows)
Download and run the installer. You need the JDK, not the JRE.


---------------------
2. Configure Java path
   check in C:\Program Files\Java to see the exact path where java was installed. Should be something like:
   C:\Program Files\Java\jdk-21

Now edit the file of the attack-vector installation. Assuming you used C:\AV it will be C:\AV\setenv.ps1

In this file you will see some paths that point to a java installation. Fix line 11 so that it points to
your Java location


---------------------
3. Maven
   We are going to build the latest version of AttackVector using Maven. Google download maven and install the latest maven-3


You want un-zip the zip file in the C:\AV so that you get a C:\AV\maven-3.x.y folder.


---------------------
4. Configure Maven path
   Check the C:\AV folder for the exact maven path, which should be something like:
   C:\AV\apache-maven-3.6.0

Now edit the file of the attack-vector installation. Assuming you used C:\AV it will be C:\AV\setenv.ps1

Fix line 12 so that it points to your maven location


---------------------
5. MongoDB
   https://www.mongodb.com/download-center/community
   (Tested with version 3.4, might work with higher versions, instructions are for version 3.4)
   Install with default values, choose: 'Complete' when asked.


---------------------
6. Configure and start MongoDB
   6.1 Create the directories: C:\data and C:\data\db
   6.2 edit the file mongo startup file of the attack-vector installation.
   Assuming you used C:\AV it will be C:\AV\_attack_vector\mongod.bat

Check that the path in this file is correct.

6.3 open a command prompt and type:
cd \AV\_attack_vector
mongod

You should now see some text ending with "... [thread1] waiting for connections on port 27017"

6.4 (Leave this command prompt open while running attack vector. This is your database.)


---------------------
7. Create a MongoDB user
   7.1 open a command prompt and type:
   "C:\Program Files\MongoDB\Server\...\bin\mongo"

you should now see something like:
2019-04-02T15:24:14.994-0700 I CONTROL  [initandlisten]
>

7.2 type:
use admin

you should see: switched to db admin

7.3 type:
db.createUser( { user: "av2",
pwd: "av2",
roles: [ "readWrite"] },
{ } )


You should see: Successfully added user: { "user" : "av2", "roles" : [ "readWrite" ] }

7.4 (close this command prompt)

---------------------
8. Compiling the source code & running
   8.1 open a command prompt and type:
   cd \AV\_attack_vector
   setup
   update

This should get the latest version of attack-vector, and the start to compile it. The first time this will take very
long, and download a lot of files. It is ok for this to take 10 minutes. Subsequently this will be much faster

Hopefully, it will end with the "BUILD SUCCESS" message.

8.2 start attack vector by typing:
run


Again: the first time, a lot of files will be downloaded and it will take a few minutes. Hopefully the final message is:

... Started Application in 6.344 seconds (JVM running for 14.088)

This will mean that everything worked.

8.3 Open attack vector:
in Chrome: go to this URL: http://localhost:8080
log in as "admin" with password "77aa44"

If you got this far, you are in business!

8.4 (leave this window open while playing the game)


Success! At this point you can start exploring the web interface.


==========================================
How to start the game once it is installed
==========================================

9. Starting attack vector when the PC was rebooted.
   9.1 Start MongoDB: open a command prompt and type:
   cd \AV\_attack_vector
   mongod

(leave this window open while playing the game)

9.2 Start Attack Vector: open a command prompt and type:
cd \AV\_attack_vector
setup
run

(leave this window open while playing the game)


---------------------
10. Updating attack vector:
    10.1 If you need to update to the latest version: go to your window running attack vector and type CTRL-C to stop it.
    10.2 Then type:
    setup
    update

You will first see if new files were downloaded, and a "Press any key to continue . . . "
If there were no errors, press any key. If there were big errors, press CTRL-C to quit.
Assuming there were no errors: the script will continue to build Attack Vector, hopefully ending with "BUILD SUCCESS"

10.3 you can then run it by typing:
run

(leave this window open while playing the game)






