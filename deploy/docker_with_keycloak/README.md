# Setting up Attack Vector with Keycloak for self hosting

## Prerequisites

- Docker

## Current pre-set

The current setting allow you to run it directly on your computer for testing.
Keycloak will be available on [auth.localhost](http://auth.localhost) and Attack Vector
on [hack.localhost](http://hack.localhost)

Keycloak is pre-configured with a realm `larp` and a client `attack_vector`

New user will get the non_hacker role

## Steps

1. Change value of `DOMAIN` from [.env](./.env) to match your domain
2. Change value of `KEYCLOAK_ADMIN` and `KEYCLOAK_ADMIN_PASSWORD` from [.env](./.env) to define the userName and
   password of Keycloak admin user
3. [Configure certificates (optional)](#configure-certificates)
4. Run docker compose -d
5. [Configure Keycloak (optional)](#configure-keycloak-optional)
6. [Configure Attack Vector](#configure-attack-vector)
7. Try your config by accessing [hack.domain](http://hack.domain), you should be redirect to Keycloak for login or
   registration as hacker
8. Have fun

### Configure certificates

If you host attack_vector on a LAN, you can skip this step

To enabled https, uncomment the folling section in [traefik.yml](./conf/traefik/traefik.yml) 
```
#    http:
#      redirections:
#        entryPoint:
#          to: websecure
#          scheme: https
#          permanent: true
#  websecure:
#    address: ":443"
#    http:
#      tls:
#        certResolver: letsencrypt
```

Then update the `certificatesResolvers: letsencrypt` with the configuration relative to your dns provider.
In the provided exemple I'm using [dynu](https://www.dynu.com/en-US/), the confidential information like username,
password, apiKey, ... can be provided as environment viriables by the [letsencrypt.env](letsencrypt.env) file.

More information [here](https://doc.traefik.io/traefik/reference/install-configuration/tls/certificate-resolvers/acme/#dnschallenge)

### Configure Keycloak (optional)

Login as admin to [admin console](http://auth.localhost/admin/master/console/) using username and password define
in [.env](./.env) (KEYCLOAK_ADMIN and KEYCLOAK_ADMIN_PASSWORD)

If you have change the attack vector url (domain or submain) you need to update manually it in the Keycloak
configuration.
Click on `Manage realms` then `larp` (or your custom realm name) then `Clients` in the side menu.
Select `attack_vector` and update the `Valid redirect URIs` and `Web origins` fields with your domain

#### Users infos

For integration with Attack Vector, the users need the following attributes:

* userName
* characterName

To modify the user attributes click on `Manage realms` then `larp` (or your custom realm name) then `User Profile` tab

#### Roles

You can define what role new users will automatically have by clicking on `Manage realms`
then `larp` (or your custom realm name) then `Realm setting` and go to `User registration` tab.
Click on `Assign role` and select `Client roles`.
You can now select the role you want to assign as default role (HACKER, SITE_ADMIN, GM)

For more infos on roles see the [Roles and skills](#roles-and-skills-information) section.

### Configure Attack Vector

1. Log as Admin following instruction `First time login as admin` from the main [README.md](README.md)
2. Set `Login: path` to `/login-oidc`
3. Set `OpenId Connect: Url` using url to keycloak openid-connect apis (
   `http://auth.${DOMAIN}/realms/${KEYCLOAK_REALM_NAME}/protocol/openid-connect`)
4. Set `OpenId Connect: Client Id` using client_id define in Keycloak (défault: attack_vector)

## Roles and skills information

The open-id connect implementation and the Keycloak pre-set have 2 roles configured

By default new user don't have role attributed to them.
They are **Non-professional hacker**<br>
They can't start a new hack or use the `scan` command.
They must be invited to a hack by a hacker.

### HACKER:

**Professional hacker**<br>
They can initially access a site and then start a new hack.
The `scan` command give them an advantage on non-professional hacker.

They have the following skills :

* SEARCH_SITE
* SCAN

### SITE_ADMIN:

**Professional hacker, that can create new site** <br>
They have the same skills has HACKER but can also create new sites.

They have the following skills :

* SEARCH_SITE
* SCAN
* CREATE_SITE

### GM:

### Add a role to a user

1. Click on `Manage realms` then `larp` (or your custom realm name)
2. Click on `Users`
3. Click on the user you want, then on the `Role mapping` tab
4. Click on `Assign role` then `Client roles`

### Set a user as GM for your larp

Follow the steps of [Add a role to a user](#add-a-role-to-a-user) and select the `GM` role