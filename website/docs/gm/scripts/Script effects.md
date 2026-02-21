---
sidebar_position: 3
---
# Script effects
This page lists all of the current effects that a script can have

## Useful effects
### Automatically hack a specific ICE layer
When adding this effect, you must specify the exact ICE layer that this effect will hack. This takes the form: `node-<ID>:layer-<ID>` for example: `node-1234-1234:layer-1234`.

### Automatically hack any ICE
This is the most powerful ICE hacking script effect it has no limitations. Any ICE will be hacked, regardless of strength or type. It's not advised to make this script generally available to the players for daily use.

### Automatically hack ICE of a specific type
When adding this you can choose which type to automatically hack. This works regardless of the strength of the ICE.

### Automatically hack ICE with low enough strength
When adding this script effect, you can choose what the maximum ICE strength is that this script can hack. In addition, you can also choose what types of ICE this effect applies to. It will never apply to password ICE, as that has no strength. By default it will not apply to Tar ICE, as its strength works differently from other types of ICE.

### Delay tripwire countdown
When running this script in a node with a tripwire that has an active countdown timer, this timer will increase to give the players more time.

You can configure how much extra time this effect provides.

### Hack below non-hacked ICE
Consider a node with these layers:

> 0. OS
> 1. Database with information
> 2. Keystore with password for ICE in layer 3
> 3. ICE with high strength

Normally hackers cannot hack layers 0-2 until they have hacked the ICE in layer 3. When running this script they can hack below the ICE (one time). Here they could either hack layer 1 to gain the information, or hack layer 2 to gain the password for the ICE.

Note that players will not see what the layers are, so the players will see:

> 0. (shielded by ICE)
> 1. (shielded by ICE)
> 2. (shielded by ICE)
> 3. ICE

So if you are adding this effect to your game, it might be better to not have too many layers below the ICE.

### Interact with script layer
This effect allows the script to interact with specific layers in a web site. This way you can simulate that hackers need specific tools (i.e. scripts with this effect) to be able to achieve certain things. For example: infect a machine with a very specific virus.

To set this: define the 'interaction key' for this script effect. In our example, this could be MACHINE_INFECTION.

Then, in your site, add a script-interaction-layer. In this layer you set the interaction key to MACHINE_INFECTION, and specify the text that will be shown when the players run the correct script.

Running a script with this effect on layers that don't have the corresponding interaction key will fail, but will not consume the script.

Note that when running this effect successfully will display a text to the players. It will not inform the game master that this has been done. If there is no game master overseeing the hacking, you can work around it by adding an off-game note in the text to inform the game master that the script was run successfully (optionally with some code word).

### Jump to node
Teleports the player to another node. The hackers must have scanned this node, so they cannot just blindly guess the node ID.

When adding this effect, you can choose if this will be blocked by ICE along the way. Note that there are ways to scan/reveal parts of the site (or the whole site) without having to hack the ICE. One such method is to first partially hack the site and then have it reset.

- When blocked by ICE, this script effect provides some utility to allow hackers to quickly move around, possibly preventing triggering tripwires.

- When not blocked by ICE, this script provides serious ICE bypassing capabilities.

### Jump to hacker
Teleports the player to another player that is currently in the same hacking run. When adding this effect, you can choose if this will be blocked by ICE along the way.

The effectiveness of this script depends on how good other players are at bypassing ICE.

### Minesweeper - unblock
This unlocks a player who clicked on a mine in minesweeper ICE.

### Rotate ICE - change ICE type
This effect changes an ICE layer, making it a different type of ICE:
 Word search -> Tangle -> Netwalk -> Minesweeper -> Word search

This script allows players to change types of ICE they don't like into types that suit them better. In addition, they may have additional scripts that work only for specific types of ICE.

### Scan node with ICE
This allows scanning beyond a node with ICE. Normally scans are blocked by nodes with ICE. Running a script with this effect allows scanning a node with ICE as if that ICE is not there. This will reveal part of the site behind that node.

### Show message
This effect shows a message in the terminal to the hacker. This message can be purely flavor (for example) `This script was written by Sl|x`.

Or it can be meaningful: informing the players that some additional effect was triggered that is not implemented by Attack Vector, but will be seen by a GM. For example the text `hacker position data transfer completed.` could be added to indicate that by running this script, they have revealed their location to some outside party.

### Site stats
This script effect will reveal information about the current site:

- the number of nodes
- the number of cores
- the number of tripwires 
- for each ICE type the amount and highest strength

### Tangle - reveal clusters
This will mark all connection points in a tangle ICE to indicate which cluster they belong to. This makes it much quicker for multiple hackers to solve this ICE together.

### Word search - show next words
This will show the next words that need to be found in a word search puzzle. This allows for players to look for more than just a single word, allowing better collaboration with multiple players.

When adding this effect, you can choose how many words will be revealed.

## Drawback effects
### Decrease future timers
This effect reduces future times that are triggered by the site. It's raising the alarm state of the site. 

For example:
- A specific timer normally takes 10 minutes
- A script contains this effect and reduces timers by 1 minute
- This script has been run 3 times
- When the timer is triggered, it now takes 7 minutes to complete.

Once a site is reset, the alarm state also resets, and subsequent timers again take their normal duration.


### Hidden effects
This effect hides any subsequent effects from hackers.

This can be used to make scripts that are actually bad for hackers, like this:

> 0. Automatically hack ICE of a specific type: Wordsearch
> 1. Hidden effects
> 2. Show message: Your location is sent to an external party.
> 3. Start reset timer: 1 minutes

What the players will see, is this:

> 0. Automatically hack ICE of a specific type: Wordsearch
> 1. Unknown effect(s)

Note that the additional effects after the hidden effects don't have to be bad. In fact they don't even need to be there at all. Just having 'Unknown effect(s)' will already impact how players feel about this script.


### Speed up reset timer
This effect only applies to timers started with the 'Speed up reset timer' effect. If such a timer has not been started, nothing happens.

If such timer has been started, its time is decreased by the amount specified for this effect. If the time runs out because of this, the site instantly resets.

### Start reset timer
This starts an unstoppable reset timer. When adding this you choose the timeout for this reset.

This timer is independent from other timers started by tripwires, and cannot be stopped by hacking a specific core.

Running this effect multiple times has not effect. When such a timer is already running, this effect does nothing.