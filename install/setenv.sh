#!/bin/sh

export MONGODB_URI=mongodb://attackvector2:attackvector2@localhost/admin
export MONGODB_NAME=attackvector2

export ENVIRONMENT=undefined
export ADMIN_PASSWORD=disabled
export GOOGLE_CLIENT_ID=none

#Set timezone, only used for logging
sudo timedatectl set-timezone Europe/Amsterdam
