---
sidebar_position: 2
---
# Ram and script loading
To be able to use a script, it must first be loaded into memory (RAM). Every script has a size in RAM blocks, and your total RAM limits the number of scripts you can load.

If you don't have the Script RAM skill, you don't have any RAM to load scripts in and cannot use scripts.

# Loading scripts
Before starting a hacking run, you must load your scripts. You can load scripts from the **Scripts** page. Here you can see how much RAM you have available and what scripts you have. You can load a script with this button:

![](../../assets/players/scripts/load-script.png)

Scripts are loaded instantly, so you can do it right before you start a hacking run. The scripts you have loaded are those that you will have available during the run. You cannot load scripts during a hacking run[^1].

![](../../assets/players/scripts/script-loaded.png)


Once a script is used, the memory remains used for some time afterwards. Used memory blocks will slowly refresh over time. During this time, this memory cannot be used to load new scripts.

![](../../assets/players/scripts/ram-refreshing.png)
*The two RAM blocks of the 'stats' script are slowly refreshing and cannot be used to load new scripts.*

Depending on how it's configured for your Larp, there may also be a period of time after starting a hacking run, in which you cannot load scripts. This is to prevent players from logging out of a hack, loading new scripts and logging in again.



[^1]: Loading scripts during a hacking run is turned off by default in Attack Vector. But it can be enabled by your Larp organizers, so perhaps in your Larp it _is_ possible to load scripts during a hacking run.

