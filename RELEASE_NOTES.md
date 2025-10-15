# Release notes AV 2.12.0 (15 October 2025)

## Features
- Script credits, script market and daily income

Hacker can have the skill to collect script credits. These can be used to buy scripts from the market.
GMs can populate the market by making scripts available to hackers at a specific price.

Hackers can receive credits in two ways:
- A new layer is added (Credits Source) that contains data that can be stolen and sold for script credits.
- The hacker can be awarded a 'daily' income of script credits as part of their skill.


# Release notes AV 2.11.1 (22 August 2025)

## Bugfixes
- Moving back to a previous node is no longer blocked by ICE in the current node.


# Release notes AV 2.11.0 (20 July 2025)

## Features
- Add skill: jump. Allows hacker to jump to the node of another hacker, ignoring ICE.
- Add skill: rollback. Allows hacker to undo their last move if that triggered a tripwire.


# Release notes AV 2.10.1 (21 May 2025)

## Bug fixes
- Resetting Minesweeper ICE no longer causes duplicate key errors.


## Features
- Tripwires can now be linked to a core in another site. Hacking the tripwire layer will reveal that site's name.


# Release notes AV 2.10.0 (27 Apr 2025)

## Features
- Tripwires can now be linked to a core in another site. Hacking the tripwire layer will reveal that site's name.


# Release notes AV 2.9.0 (16 Apr 2025)

## Features
- New skills: Bypass, Stealth and Weaken.

### Bypass
This skill allows the hacker to ignore ICE in the first node of a site with respect to moving beyond this ICE. This also
allows the hacker to perform a scan inside the node to reveal the site beyond this node. Normally the ICE blocks 
revealing the rest of the site.

### Stealth
This skill increases the time for a site to shut down when the hacker triggers a tripwire. A stealth value of +30% means
that the total time before shutdown is increased by 30%. For example from 10 minutes to 13 minutes. This skill can also
be applied negatively, to decrease the time. A stealth value of -30% will decrease the time from 10 minutes to 7 minutes.

### Weaken
This skill allows the hacker reduce the strength of a single layer of ICE. The strength is reduced by one step, for 
example Very Strong becomes Strong. This skill works on one or more types of ICE, specified when setting up the skill. 
You can have this skill more than once. Where each instance defines what types of ICE it affects. Each instance can be 
used once per site. After a site is reset, the skill (instances) can be used again on this site.

## Changes
- The screen for managing skills has changed to allow adding multiple instances of the weaken skill


# Release notes AV 2.7.1 (09 Apr 2025)

## Changes
- You can no longer choose the number of clusters for Tangle ICE. This is now always based on the ICE strength


Fixing the number of clusters makes the difficulty consistent, and prevents GMs from accidentally creating ICE that is 
_way_ harder than intended.
It also simplifies gathering statistics for Tangle ICE.

Number of clusters:
- very weak  : 1 cluster, no changes.
- weak       : 2 clusters, the clusters are revealed to help hackers learn how this works
- average    : 3 clusters, not revealed.
- strong     : 3 clusters, not revealed
- very strong: 4 clusters, not revealed
- onyx       : 4 clusters, not revealed


# Release notes AV 2.7.0 (09 Apr 2025)

## Features
- Collect and export statics for how long it takes to hack ICE


# Release notes AV 2.6.0 (06 Apr 2025)

## Features
- Scripts, including 19 script effects.
- Pagination and filtering for users

See Youtube for a video explanation of scripts:
- for players: https://youtu.be/ri3hewshbKA
- for GMs: https://youtu.be/3BBU9aGHK6U

## Bugfixes
- Add brute force protection for admin login


# Release notes AV 2.5.2 (29 Dec 2024)

## Bugfixes
- Fix broken Netwalk Very strong and Onyx ICE puzzles
- Fix login and redirect to ICE puzzle 


# Release notes AV 2.5.1 (13 Nov 2024)

## Features
- Detect screen size mismatch. Adjust zoom (Chrome) or prompt user (once per month).


# Release notes AV 2.5.0 (11 Nov 2024)

### Important: this version relies on Java 21. 
The installation instructions already instruct to install Java 21, but if you have an older version, you need to update to Java 21 before updating to the latest version.

### Important: re-configure your installation and master password
Configuration has moved from the 'setenv.sh' / 'setenv.bat' file to the database. 
You will need to manually configure your server again by logging in as 'admin'. Values from your setenv file will be ignored.

- The most important value is the master password. When you update, you will have a blank master password and everyone will be able to log as anyone.
Set the master password immediately after upgrading.
- The creation of a tutorial site (for new players) is now turned off by default. You can restore previous behavior by setting the value of the "tutorial site name"
  to "tutorial". 

## Features
- Most configuration now resides in the database
- Support for skills added, starting with 3 skills
- Allow customizing or disabling tutorial site (see configuration items as admin)
- Release notes are linked from the login screen

## Changes
- Mandatory users (system, admin, gm, template) are protected from change or deletion
- Default hackers (hacker, Stalker, Paradox, Angler) can now be deleted and will no longer be automatically recreated on startup.
- Tutorial site can be deleted and will no longer be automatically recreated on startup.
- Local files now always reside in the "local" folder of the server, relative to the "run.sh" script. This prevents possible unsafe server configurations and also simplifies the configuration.

## Bugfixes
- Sites can no longer be renamed to existing site names

## Details
### Configuration
Starting with this version, all configuration (except database details) now reside in the database. 
It can be managed at runtime by an admin user, there is always a user named 'admin'.

Previously, configuration was partly tied to the larp that was running AV2, and this was partly derived from the URL.
This was not a scalable solution. Also, it's nicer to be explicit about what configuration options there are.

### Skills
There is basic support for skills. These are meant to differentiate users of varying expertise at hacking.
See Youtube for an explanation: https://youtu.be/gu0Fx9m9M1g

You can assign or remove skills from hackers when logged in as a gm, via the user's menu.

There is a new user named "template". This user is the skill template for new hackers. Any skills the template has, will automatically be assigned to new users.
This way, if certain skills are default for all users, you will not have to manually assign them.

At present there are three skills, which are all existing features in the game. By removing skills from users, those users are not full hackers.
The skills are:
- Scan
- Search site
- Create site.

By default, all existing hackers will get the skills Scan and Search Site. This is also the default setting for the template user.

See the website for details on the skills: https://attackvector.nl/website/players-skills

Note that skills are optional, by default they are not shown to the hackers. Enable this in the configuration.

Skills are a work in progress. There will be many more skills in the future. If you have specific skills you would like to be added to the game, please let me know.

---

# Release notes AV 2.4.0 (1 Aug 2024)

## Features
- Implement Minesweeper puzzle
- Add Clusters to Untangle
- Checking for unconnected nodes as part of site validation (preventing bugs when hacking it)
- Sites with errors cannot be made hackable

## Bugfixes
- Images of other hackers no longer move up during a hack
- No longer need to log in to view widgets

## Tech improvements
- Major refactoring of code package structure. This is reflected in the database
- Improve install script to make Mongod auto start at server boot

---

# Release notes AV 2.3.0 (17 Jul 2024)

## Features
- You can now embed links in text-nodes. Format: [http://example.com]link[/]
- You can now embed pictures in text-nodes. Format: !http://<link to image>!
- You can store files on the AV server, and access them via https://<your-domain>/local/<your-file>
- You can combine the above, for example: [http://<your domain>/local/img/it-large.png] !http://<your domain>/local/img/it-small.png![/]

---

# Release notes AV 2.2.0  (23 Feb 2024)

## Features
- Hackers can make sites (go to the sites tab at the bottom)
- Import/export of sites
- Map size increased. Beware of bigger sites (by SL request)
- Tangle is now in dark mode
- Moving to an un-scanned node now auto-scans the node
- /share allows multiple users. Like: /share hacker1 hacker2
- Command 'help' improved
- Command 'scan' no longer requires a node id, now works the same outside and inside the site.
- Command 'connect' is renamed to 'password'. It is only used to enter the password interface of ICE
- Hacking tripwire now reveals the node-id that contains the core that resets the tripwire.
- Manage hackable of site via site-list

## Bugfixes
- Scanning should now work correctly
- Removed unimplemented pages (Mail, SL: logs)
- User name validation added
- Error popups are easier to dismiss
- Redirect to login page instead of showing an error if not logged in
- Fixed stuttering of terminal
- Disabled CTRL-F for word-search on Firefox
- Removed broken link to manifest.js
- No longer need to be logged in for widget

## Tech improvements
- Mongo database has schema versioning
- Attempt to improve stability (move command not working)
 
