---
sidebar_position: 3
---
# Internal sites and uplinks
You can build a network of sites where some sites can only be reached from inside another site. For example: the hackers
first need to break into a corporate gateway before they can reach the research lab behind it.

## Internal site
In the site properties, check **Internal**. An internal site cannot be reached from outside:

- When a hacker types its name on the home page, they get "Site not found", just like for a site that does not exist.
- Runs on an internal site are not shown on the home page, and cannot be shared with `/share`.

The site still needs to be hackable, like any other site.

## Uplink layer
Add an **Uplink** layer to a node in the outer site, and choose the internal site as the target site. When a hacker hacks
the uplink, they arrive in the start node of the target site.

A site can have multiple uplinks, and an internal site can have uplinks to other internal sites, so you can chain sites:
gateway → lab → vault.

## Back doors: an uplink to another node
Fill in the **Target node** of the uplink to let hackers arrive at that node instead of the start node. This way an uplink
can be a back door into a protected part of a site, skipping the heavy ICE at its front door. The target site does not
have to be internal: it can be a normal site that hackers can also attack from outside.

- Hackers arriving at the target node scan the site outward from there. ICE they find from behind blocks the scan like
  it does from the front.
- The Bypass skill works on ICE in the node where the hacker arrives, not on the start node.
- Network switches work from the node where the hacker arrived: a hacker is only cut off when a dropped route separates
  them from that node.
- If a hacker attacks the site from outside later, in the same run, the start node is shown as well.

## What happens in a run
- When a hacker hacks an uplink and already has a run on the target site, that run is continued. Otherwise, a new run is
  created.
- Hackers that share a run on the outer site will also share the run on the target site.
- When a hacker is disconnected from the target site (`dc`, a site reset, a GM refresh), they return to the node with the
  uplink in the outer site. The tripwires, daemons and other layers in that node trigger again on their return.
- They end up outside the outer site instead if the outer site was shut down in the meantime, or if a network switch has
  dropped the route between the node where they entered the outer site and the node with the uplink. Hackers that stayed
  in the outer site would have been cut off by that route as well.

## Exporting and importing
When exporting and importing, the uplink remembers its target by site name and its target node by network id. Import the
target site first, then the site with the uplink.
