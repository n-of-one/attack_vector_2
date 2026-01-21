# Creating Scripts
As a game master you will have to create the scripts that exist in your game world. There are no fixed or pre-defined scripts to start with.

You can create a set of scripts that are generally available to the players. And you can also create custom scripts for specific plots.

## Script effects
Each script consists of one or more effects. These effects can be useful or be a drawback. For a full list of script effects, see [here](Script%20effects.md)

**Useful** script effects include things like automatically hacking ICE or extending a tripwire timer.

**Drawback** effects include things like starting a site reset timer. Adding a drawback to a script is optional. 

## Size
Each script has a size in RAM blocks. Choose the size to roughly match how strong the script is.

## Examples
### Mask
This script has the following effect:
- Increase site reset timer by 5 minutes
- Decrease future timers by 1 minute

Size: 1

This script gives the players a little extra time. The drawback makes that it's less useful in very large sites to use in the beginning. Depending on the site this can be irrelevant or very annoying.

### Icepick
This script has the following effects
- Hack one layer of Tangle ICE
- Start a countdown timer of 15 minutes

Size: 4

This script allows hackers to completely bypass any one layer of Tangle ICE. This is quite powerful if used to overcome very strong or onyx strength ICE. To compensate, the automatic countdown timer makes it so that this script is only useful near the end of larger sites and cannot be used to hack the first strong Tangle ICE in a larger site.


## Script design
You can choose how many scripts you want in your world. Having many powerful scripts will change the way how players hack sites, relying on scripts to circumvent ICE instead of solving the puzzles.

Each script must have a useful effect, otherwise players will not use it. Adding a drawback effect can be used to spice up scripts, but is optional.

The drawback effect: "Hidden Effect" is special. It does nothing on its own, but it does hide any additional effects from being known by the players. Players will see that this is part of the script, so they will trust this type of script less. This can be used if the players did not write the scripts themselves. Even if there are no additional effect, this will still impact the perception of that script type by the players.

## Multiple effects
When adding multiple effects, each effect will be applied when the script is run, starting with the first (top most) effect and going down the list.

## Starting and speeding up reset timers
One specific drawback is that of starting a reset timer ([see here](Script%20effects#Start%20reset%20timer)). When this effect is triggered, an unstoppable countdown will start that will reset the site once completed. So running this script will limit the time the players have left in this site.

This can be used to create a powerful script that hacks ICE, at the price of reducing the time the players have left in the site. For example, you could set this timer at 10 minutes.

In order to prevent players from just acquiring a lot of these scripts, and spamming them to hack all ICE in those 10 minutes, you can add a second drawback: Speed up reset timer ([see here](Script%20effects#Speed%20up%20reset%20timer)). This will speed up an existing timer -started by the previous effect- by x minutes. By also adding this to the same script, you can prevent players from being able to spam this script.

But you do have to take care in which order you add these effects.

**- Wrong order of effects -**

> 0. Useful ICE hacking effect
> 1. Starting a reset timer (10 minutes)
> 2. Speed up reset timer (5 minutes)

When running this script, a timer is started for 10 minutes, and immediately reduced by 5 minutes. So this ends up with a 5 minute countdown timer.

So instead make the order like this:

**- Correct order of effects -**

> 0. Useful ICE hacking effect
> 1. Speed up reset timer (5 minutes)
> 2. Starting a reset timer (10 minutes)

The speed up effect (2) will have no effect on other types of timers, and will do nothing if effect 3 has not triggered yet.
