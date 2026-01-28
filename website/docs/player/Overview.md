---
sidebar_position: 1
slug: /
---

# Overview
As a player you, are a hacker infiltrating the computer systems of the game world.

## Sites
These hackable systems are called **sites** in Attack Vector. You find site names in the game world, or receive them from a game master. Once you know a site’s name, you can attack it.

## Hacking run
Attacking a site starts a new **hacking run**. You can invite other hackers into your hacking run so you can see each other.

Other hackers may also be hacking the same site, but if they are not part of the same hacking run, you will not see them. You will still see any effects they have on the site, as it is shared.

During a hacking run, you typically:
- Share the hacking run with fellow hackers
- Explore the site and assess its defenses
- Encounter obstacles that block progress
- Choose which obstacles to bypass or defeat
- Overcome obstacles through puzzle minigames
- Race against time
- Extract the information you are looking for
- Exit before the system resets or locks you out

If you fail, you are locked out of the site for a period of time. Afterward, you can try again, but you must overcome the same obstacles.

## Nodes
A site is made up of **nodes** that are connected to each other. You start at the entry node of the site, also known as the first node. Often this node has the address '00'. You move from node to node to go deeper into the site. Along the way, you encounter nodes containing obstacles or information you seek.

In the UI, nodes are shown on a map like this:

![sitemap](../assets/players/sitemap.png)

This site contains 6 nodes. The red frog diamond indicates your position in the site. The yellow diamond is another hacker.

## Layers
Each node contains one or more **layers**. Layers are the elements you can directly hack or interact with. Examples of layers include:
- Databases that contain information
- Protection systems like ICE or tripwires
- Management systems like cores or keystores.

In the UI, you inspect layers by clicking a node on the map.

![Picture of layers in a node](../assets/players/layers.png)

This node contains 4 layers (0-3):
- Layer 3 is an HR database that can be hacked to find information about employees.
- Layer 2 is an ICE layer that can be hacked by playing a minigame: word search.
- Layers 1 and 0 are shielded by the ICE layer, so you cannot see what they are.

More info: [Layers overview](layers/Layers%20overview.md)


## ICE
Some layers are ICE layers. These will block your progress through the site.

To reach deeper into the site, you need to hack the ICE layers. Hacking an ICE layer starts a puzzle minigame.

The following ICE layers exist, each with a corresponding minigame:
- [Jaal (Word search)](ICE/Word%20Search%20ICE.md)
- [Visphotak (Minesweeper)](ICE/Minesweeper%20ICE)
- [Gaanth (Tangle)](ICE/Tangle%20ICE.md)
- [Sanrachana (Netwalk)](ICE/Netwalk%20ICE.md)
- [Rahasy (Password)](ICE/Password%20ICE)
- [Tar (tar)](ICE/Tar%20ICE)

## Timers
Some layers will be 'tripwire' layers. Entering a node with a tripwire layer starts a timer. If the timer expires, the site resets and all progress is lost. All hackers are ejected from the site and all ICE must hacked again.

Tripwire timers can be reset by hacking the corresponding 'core' layer.

For more details, see [here](layers/tripwire).

## Terminal
You hack a site by entering commands in the terminal. Only a few commands are required, such as `move` or `hack`. You can always use the command `help` to see the available commands.

More info: [Terminal commands](Terminal%20commands)

## Skills
Skills allow some hackers to be better at hacking than others. More details can be found [here](gm/users/Skills.md).

Check with your organizers which skills are available.

## Scripts
Scripts are one-time-use programs that assist you during hacking runs. More details can be found [here](Scripts/Scripts%20Overview).

Check with your organizers if scripts are used on your Larp.
