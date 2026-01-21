---
sidebar_position: 2
---
# Tripwires and cores
By adding a tripwire layer, you add a timer that adds urgency and introduce the risk of failing. The players must complete their task before the timer runs out. Usually this means having to hack one or more ICE layers within that time.

# Tripwire timer
When a hacker enters a node that has a tripwire, a timer will start. This timer is visible for all hackers. There is no way to prevent the tripwire from starting. But once a certain tripwire has started a timer, any hacker can safely enter this node, this will not start a new tripwire.

If the timer runs out, the site resets. All hackers will be disconnected (pushed to the outside) and there will be a period during which hackers cannot connect to the site.

After that period has ended hackers, can attack the site again. But all the ICE will have reset, meaning that the hackers will have to hack it from scratch. All puzzles will be new random puzzles.

## Cores
Tripwire timers are stopped by hacking the corresponding Core. So a basic linear setup is this:

- Node 00:
	- Layer 1: Tripwire
	- Layer 2: ICE
- Node 01:
	- Layer 1: Text node with info for the players
	- Layer 2: Core

You connect the tripwire in node 00 with the core in node 01. In this setup the players have limited time to hack the ICE, but if they do, they have can stop the timer and then have ample time to explore the rest of the site. In this case that's just the text layer in Node 01, but you can expand om this concept.
