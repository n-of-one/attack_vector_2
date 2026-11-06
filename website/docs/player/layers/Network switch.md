---
sidebar_position: 13
---
# Network switch
This layer makes the hacker choose which way to go: one of the connections leaving the node, for example east or west.

Until a choice is made, these connections are shown in amber on the map and cannot be used. Scanning past them is possible,
so hackers can see what lies behind each path before choosing.

```
⇋ move 05
blocked Network switch in current node: choose a path first: hack 1.
```

## Hacking
Hacking the network switch shows the paths. Add the name of a path (or the network id of the node it leads to) to choose it.

```
⇋ hack 1
Choose the path to open. This choice is permanent.
  hack 1 east - opens the connection to 05
  hack 1 west - opens the connection to 08

⇋ hack 1 east
Network switch in 04 set to east. Connection severed to: 08
```

The choice is made for everyone in the site. The connections that were not chosen are severed: they are shown greyed out
with a ✕ on the map, and nobody can move through them anymore.

The choice is permanent. A site reset does not undo it.

Scripts cannot get past a severed connection either: jumping to a node, or running a script on a node that can only be
reached through a severed connection, is refused. Normal scans still go past it, so the severed branch stays visible.

## For the GM
In the editor, give each connection that is part of the choice a path name. Connections without a name are not affected.
The editor also shows which path was chosen, and can set it back to "not chosen" to undo the choice.
