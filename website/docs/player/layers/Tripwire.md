---
sidebar_position: 4
---
# Tripwire
The Tripwire layers start a site-reset countdown timer when you arrives in the node.

When the timer reaches zero, the site will shut down for some time. All hackers are kicked out of the site. During the shutdown no connections to the site are possible. After the shutdown ends, the site can be attacked again. But all ICE puzzles are reset and have to be hacked again.

Tripwire timers can be stopped by hacking the right core layer, see [here](core). Hacking a tripwire will reveal in which node the core is located.

```
⇋ move 02 
Entered node 02 
Layer 1 triggered site reset in 19 minutes 30 seconds. 

⇋ view 
Node service layers: 
0 OS 
1 Tripwire 

⇋ hack 1 
This tripwire is managed by core in node 03
```

Note that not all tripwires will be linked to cores. If a tripwire is not linked to a core, it's timer cannot be reset.

