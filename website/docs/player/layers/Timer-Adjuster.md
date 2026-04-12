---
sidebar_position: 6
---
# Timer Adjuster
This layer will speed up or slow down existing tripwire timers.

```
⇋ move 09
Entered node 09
Timer accelerator speeds up existing shutdown timers.
Countdown accelerated by 1 minute
```

If there are no running timers, this layer has no effect. Only tripwire timers are affected, not timers started by scripts.

When the same or a different hacker enters the node (again), the effects depends on how the layer is configured. Options are:
- The effect takes place every time a hacker enters the node
- The effect takes place once for every new hacker
- The effect takes place only once

If the site is reset, then the timer adjuster will trigger again on new entry.

## Hacking
Hacking reveals how this layer works. The same information can also be found by clicking on the node.

```
⇋ view
Node service layers:
0 OS
1 Timer accelerator

⇋ hack 1
Hacking Timer accelerator reveals that this layer:
- speeds up shutdown timers by 1 minute
- only triggers once
```