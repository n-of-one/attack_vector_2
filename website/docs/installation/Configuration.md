---
sidebar_position: 2
---
# Configuration
There are many configuration items, but only a few are needed for normal operation:

- Password
- Login setup
- Larp name

This will give you a default installation. You might want to go over the hacker related configuration items, to set these for your Larp.

Development and Larp specific settings are not relevant for normal operation.

## Login
### Password
The single password used for game masters and admins. This is used in the generic login screen, see [here](/gm/GM%20login).

Default: (empty)

General advice: set this to a strong password that is not used anywhere else. 
Attack Vector uses a single password in order to discourage using personal passwords on this site.

### Google client ID
This is the client ID for google login.

Default: (empty)

This is the (non secret) google client ID. If you want to use Google login, you must enter this value. It looks like this: `(numbers)-(letters).apps.googleusercontent.com`

To create your Google client ID, follow the guide, see [here](/installation/Google%20authentication).

If you need to retrieve it again, find your Google client IDs in the list of "OAuth 2.0 Client IDs" on Google's Credentials page page: [external link](https://console.cloud.google.com/auth/clients).

### Path
The path used for login.

Default: /login (this is for using Google login)

Alternatives:

- /devLogin (use during development, one click login without passwords)
- /login-frontier (used for Frontier Larp SSO)

General advice: do not change this.

## Generic
### Larp name
The name of the larp.

Default: unknown

This is used in the file name of exports of sites.

## Hacker
### Delete run links
Allow the hacker to delete links to runs.

Default: true

General advice: set to true

If set to false, the hackers won't be able to remove links to runs. This is used -for example- on attackvector.nl where the system generates a single run link (to their personal tutorial site), and there are no other sites for the hacker to hack.

### Edit character name
Allow the hacker to change their character name.

Default: true

Character names are not shown to other hackers. They are only shown to GMs to help them identify the character that is associated with an AV user.

If set to false, only the GM can change their character name.

### Edit user name
Allow the hacker to change their user name. Their user name is also how they are shown inside Attack Vector.

Default: true

General advice: set to true if you want your hackers to choose their user name.

If set to false, only a game master can change their user name.

### Script loading during run
Default: false

Hackers have a script overlay where they can access their scripts during a run. For each script there are buttons to unload/share/delete their scripts. If this value is set to true, it will also include a button to load a script.
Allowing hackers to load scripts during a run will make it less predictable for game masters to know how hard a site will be.

That said, for most sites, hackers can just disconnect from the site, load scripts using the script page and then connect again. So disallowing this is more of a statement than a real prevention.

See also: Script RAM refresh duration.
See also: Script lockout duration.

### Script lockout duration

Default: 01:00:00

Scripts give hackers a way to make sites easier. To allow GMs to have some control over the amount of scripts a hacker can bring to a single site, hackers cannot load scripts for a certain duration after they have have entered a site.

This provides a minimum time window in which a hacker cannot load new scripts.
Setting this value too low will allow hackers to possibly bring multiple scripts to a single run

Setting this value too high will prevent hackers from using multiple scripts in consecutive hacks on different sites.

### Script RAM refresh duration

Default: 00:15:00

Scripts give hackers a way to make sites easier. To allow GMs to have some control over the amount of scripts a hacker can bring to a single site, the RAM of a script is locked for a certain amount of time after it is used.

This prevents hackers from quickly loading new scripts after they have used their initial set of scripts. A single block of RAM becomes available again after this duration has passed.

Setting this value too low will allow hackers to possibly bring multiple scripts to a single run

Setting this value too high will prevent hackers from using multiple scripts in consecutive hacks on different sites.

See also: Script lockout duration.
See also: Script loading during run.

### Show skills
Show the skills of a hacker to the player, when they view their user info.

Default: false

General advice: set to true if your Larp is using skills, and different hackers have different skills.

If set to false, only game masters can see their skills. This does not affect having or using skills. So if you give a hacker (or all hackers) a skill, they can still use it, just not see it.

### Tutorial site name

Default: (empty)

General advice: set to "tutorial" if you want to use the default tutorial site.

If you want new hackers to automatically start with their personal tutorial site, set the name of this site here. This site must be present. (This is how it works on https://attackvector.nl).

When installing Attack Vector, a tutorial site named "tutorial" will be set up. You can use that name here.

If you leave this empty, new hackers will start with an empty page without sites/run-links.

## Development
These configuration items exist to help during development. They can be left on their default values.

### Hackers can reset sites
Gives all players the option to reset a site.

Default: false

General advice: leave this to false.

Having a hacker be able to reset a site is mostly useful during development, where you want to quickly run test against a single site multiple times. Often you want the site to be in it's reset state, and you don't want to log in as a game master to do this.

This is also used for attackvector.nl where there is only the tutorial, and this way the players can replay the tutorial.

### hackers can use dev commands
Can hackers use commands that are only intended for developers?

Default: false

General advice: leave this to false.

There are a number of terminal commands that are very overpowered, and not intended to be part of the game. They are used to make it quicker to test during development. These commands include: quickscan (qs), quickattack (qa), quickhack, qmove, sweeperunblock.

### ICE quick playing
Skip most of the flavour text when accessing ICE.

Default: false

General advice: leave this to false.

Setting this to true will skip most of the flavour text when opening an ICE puzzle. This is convenient during development, but not intended for normal play.

### Simulate non-localhost
Artificial delay when responding to network calls

Default: 0

General advice: leave this to 0.

If you are running attack vector on a local machine, and want to know how responsive it will be when running on the cloud, you can set this value to the number of milliseconds of artificial delay you want. Suggested value: 70

### Testing mode
Enable testing mode, make ICE puzzles predictable

Default: false

General advice: leave this to false.

Setting this to true change the behavior to help with automatic testing. ICE puzzles will no longer be random, so they are predictable to solve by automated testing scripts.

## Larp specific
These configuration items exist for features of specific larps. Currently that's only for Frontier, but other Larp specific features could be added in the future.

### LOLA enabled
Default: false

General advice: leave false

LOLA is an external system used at Frontier Larp. It connects to Attack Vector as a hacker.

### Orthank token

Default: (empty)

General advice: leave empty

Orthank is a system used at Frontier Larp that stores character information. If you are not running Frontier, you can ignore this field.

