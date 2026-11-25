---
sidebar_position: 14
---
# Uplink
An uplink is a connection from this site to another site. Some sites cannot be reached from outside at all: the only way in
is through an uplink in a site that is connected to it.

```
⇋ view
Node service layers:
0 OS
1 Uplink -> research-lab
```

## Hacking
Hacking the uplink uses the connection between the two sites. Your hacking software hides your session inside the
handshake between the sites, then you arrive in the other site. This is usually its start node, but an uplink can also lead
to a node deep inside the other site, behind the ICE that protects its front door. From there you scan and explore the site
as usual, outward from where you arrived.

```
⇋ hack 1

Uplink carrier detected → research-lab
- Session request corp-gateway → research-lab
  - ok Handshake intercepted
  ...
Routing through to research-lab.

Entered node 00
```

The connection is refused when the other site is shut down or not accepting connections.

Only you move to the other site. Other hackers in your run have to hack the uplink themselves to follow you, and they will
join you in the same run on the other site.

## Leaving the other site
When you are disconnected from the other site, for example with `dc` or by a site reset, you return to the node with the
uplink in the site you came from.

```
⇋ dc
Disconnected
Uplink to research-lab dropped.
Session restored at node 01.

Entered node 01
```

If that is not possible, you end up outside that site. For example: the site you came from was shut down, or a network
switch has dropped the route between the node where you entered that site and the node with the uplink.

Leaving the run (going to the home page) breaks the connection: to go back, you have to go through the uplink again.
