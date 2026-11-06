---
sidebar_position: 13
---
# Network switch
This layer makes the hacker choose which way to go: one of the connections leaving the node, for example research or archives.

Until a choice is made, these connections are shown in amber on the map and cannot be used. Scanning past them is possible,
so hackers can see what lies behind each path before choosing.

```
⇋ move 05
blocked route to 05 held in standby by a network switch.
```

## Hacking
Hacking the network switch shows the paths. Add the name of a path (or the network id of the node it leads to) to choose it.

```
⇋ hack 1
Routing table:
  hack 1 research  → 05
  hack 1 archives  → 08
warning switch firmware locks after first route commit. Uncommitted routes will be dropped.

⇋ hack 1 research
Topology change switch 04 locked to research. Dropped routes: 08
```

The choice is made for everyone in the site. The connections that were not chosen are severed: they are shown greyed out
with a ✕ on the map, and nobody can move through them anymore.

The choice is permanent. A site reset does not undo it.

Scripts cannot get past a severed connection either: jumping to a node, or running a script on a node that can only be
reached through a severed connection, is refused. Normal scans still go past it, so the severed branch stays visible.

## For the GM
In the editor, every connection of the node is listed as a route. Check the routes that are part of the choice and give each a name. Unchecked routes are not affected.
The editor also shows which route the switch is locked to, and can set it back to "(not locked)" to undo the choice.
