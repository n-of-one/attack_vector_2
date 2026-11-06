---
sidebar_position: 13
---
# Network switch
This layer makes the hacker choose which way to go: one of the routes leaving the node, for example research or archives.

Until a choice is made, these routes are shown in amber on the map and cannot be used. Scanning past them is possible,
so hackers can see what lies behind each route before choosing.

```
⇋ move 05
blocked route to 05 held in standby by a network switch.
```

## Hacking
Hacking the network switch shows its routing table. Add the name of a route (or the network id of the node it leads to) to choose it.

```
⇋ hack 1
Routing table:
  hack 1 research  → 05
  hack 1 archives  → 08
warning switch firmware locks after first route commit. Uncommitted routes will be dropped.

⇋ hack 1 research
Topology change switch 04 locked to research. Dropped routes: 08
```

The choice is made for everyone in the site. The routes that were not chosen are dropped: they are shown greyed out
with a ⌁ on the map, and nobody can move through them anymore.

A site reset does not undo the choice. A network switch can be linked to a core, possibly in another site: hacking that core
unlocks the switch, so the routes are back in standby and a new choice can be made. Hacking a locked switch shows which core manages it.

```
⇋ hack 1
Switch locked. Route fixed to research → 05.
Lock managed by core in node 09
```

Scripts cannot get past a dropped route either: jumping to a node, or running a script on a node that can only be
reached through a dropped route, is refused. Normal scans still go past it, so the dropped branch stays visible.

## For the GM
In the editor, every connection of the node is listed as a route. Check the routes that are part of the choice and give each a name. Unchecked routes are not affected.
The editor also shows which route the switch is locked to, and can set it back to "(not locked)" to undo the choice.
Optionally choose an unlock site and core: hacking that core unlocks the switch.
