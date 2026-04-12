---
slug: /
sidebar_position: 1
---
# Installation
Attack Vector consists of a Java application that uses MongoDB for a database.

It requires at least Java 21 and is tested to work with the community version of MongoDB version 7 or higher.

You can run Attack Vector on Linux or Windows. For a Linux server dedicated to only run Attack Vector, 1 GB should be enough memory. For other systems I recommend 1 GB of free memory.

See the installation instructions for Linux (Ubuntu) [here](https://github.com/n-of-one/attack_vector_2/blob/main/install/README-linux.md).

See installation instructions for Windows [here](https://github.com/n-of-one/attack_vector_2/blob/main/install/README-windows.md).

## Running Attack Vector
The commands to manage the server are in the folder that you started in. You can start the server with: `./run.sh` or `./run.bat`

You can upgrade to the latest version from Github with `./upgrade.sh` or `./upgrade.bat`

## First time login as admin
To log in as admin, click on the `2` in the big "Attack Vector 2" banner on the login screen. This will take you to the generic login page.
Alternatively you can navigate to "/adminLogin" in the browser. This will open up a login page where you can enter username and password.
Enter username "admin" and click "sign in".

The first thing you want to do is set a master password. Find the configuration item "Password" and enter a secure password. This password will be used for everyone not using their personal account, so will likely be shared within your Larp organization. So don't use a password that is also used for anything else.

It's also good to go over other configuration items, see [here](Configuration).
