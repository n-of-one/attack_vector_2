---
sidebar_position: 10
---
# Script interaction
This layer represents an application that can be interacted with via a script. For example a life support system that can be disabled only using a dedicated script.

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

You need to have the correct script to interact with a layer of this type. There is no 'generic' interaction script.

## Effect in the game world
There is no mechanism in Attack Vector to signal to a game master that this layer has been interacted with. So if the interaction indicates that an effect in the real world has occurred, then either a game master needs to see that happening, or you need to inform a game master of this yourself.