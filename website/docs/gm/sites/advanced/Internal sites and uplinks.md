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

## What happens in a run
- Hackers that share a run on the outer site will also share the run on the internal site.
- When a hacker is disconnected from the internal site (`dc`, a site reset, a GM refresh), they return to the node with the
  uplink in the outer site. The tripwires, daemons and other layers in that node trigger again on their return.
- If the outer site was shut down in the meantime, they end up outside the outer site instead.

## Exporting and importing
When exporting and importing, the uplink remembers its target by site name. Import the internal site first, then the site
with the uplink.
