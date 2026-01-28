---
sidebar_position: 2
---

# Layers
There are different types of layers, which defines what they do.

Layers have a name that indicates their meaning. But you can't always tell the type of a layer by its name. In those cases you just have to hack the layer to find out what it does.

## OS
Every node will always have an OS layer as layer 0. It's there for flavor. If you hack it, nothing happens.

```
⇋ view 
Node service layers: 
0 OS 

⇋ hack 0 
Hacking OS reveals nothing new.
```

## ICE
Various ICE layers exist to secure underlying layers and prevent movement to other nodes.

Hacking this layer opens a mini-game in a new browser tab.

```
⇋ view
Node service layers: 
0 unknown (shielded by ICE) 
1 Gaanth ICE 

⇋ hack 1 
Hack opened in new window.
```

## Tripwire
Tripwire layers start a countdown timer when a hacker arrives in the node.

When the timer reaches zero, all hackers are kicked out of the site, and all ICE puzzles are reset. They have to be hacked again with a new puzzle if they were already hacked. Tripwire timers can be stopped by hacking the right core layer.

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

## Core
Hacking a core can reset a tripwire timer. It optionally reveals the entire site map.

```
⇋ view 
Node service layers: 
0 OS 
1 Core 

⇋ hack 1 
Hacked: 1 Core 
- revealed network 
- stopped timer for this site
```

## Keystore
Contains a temporary password of a specific ICE layer. Hacking this layer will reveal the password, but not what ICE it is for.

```
⇋ view 
Node service layers: 
0 OS
1 Keystore 

⇋ hack 1 
hacked. Password found: Gaanth/7n-43-6f-3a/3u-54-4u-1t/
```

This temporary password can be used to bypass a specific ICE layer without having to complete the mini-game. The first part of the password indicates the type of ICE this password is for. In this example 'Gaanth' is the in-game name for the Tangle ICE. You can open the password interface of an ICE layer with the `password` terminal command.

The temporary password of the ICE will change if the site is reset. 

Rahasy or 'Password' ICE layers have a permanent password that does not change when the site is reset. This permanent password not stored in a keystore layer, but must be found somewhere else.

## Status light
This layer type represent something in the real world that can be turned on and off, or locked and unlocked.

By hacking this layer you can change the status of the thing, so unlocking the door, or disabling the security system.

```
⇋ view 
Node service layers: 
0 OS 
1 Lock 

⇋ hack 1 
Opened in new window.
```
Hacking this layer opens a new browser tab that shows the switch and you to flip it..

There *can* be something in the real world that shows the current state. This will be a phone or a laptop. The state shown on the phone or laptop is changed in real time when you flip the switch.

## Text
This represent an application of the website. For example a database.
Hacking these layers reveals information found in that application.

```
⇋ view 
Node service layers: 
0 OS 
1 Database 

⇋ hack 1 
Hacked: 1 Database 
Database: 
- No records found. 
Database logs:
- delete all records (user: x44)
```

*In this example the players learn that it was user x44 that deleted all database records.*

## Credits source
This represent an application that contains data that can be stolen and sold for script credits. Your hacker needs to have the **Script credits** skill to be able to steal the data.

```
⇋ view 
Node service layers: 
0 OS 
1 Mailserver 

⇋ hack 1 
Valuable data found, worth 10⚡. Sending to data broker. 
Clearing data from layer... 

Done.

⇋ hack 1 
No data of value found. 
Logs indicate data has recently been deleted.
```

The data can only be stolen once, stealing it will remove the data from the layer.

## Script interaction
This represents an application that can be interacted with via a script. For example a life support system that can be disabled only using a dedicated script.
```
⇋ view 
Node service layers: 
0 OS 
1 Life support system

⇋ hack 1 
Unsupported protocols. This layer uses non-default protocols. 

⇋ run 9d6f-45df 1 
Executing on layer. 

Setting life-support-endable: false 
```
*The command `run 9d6f-45df 1` runs the script with id:`9d6f-45df` on layer 1.*