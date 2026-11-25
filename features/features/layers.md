# Layers

What non-ICE layers do during a run. ICE layers are described in `ice/`, tripwire, timer adjuster and core in `timers.md`.
Editor properties of all layers are listed in `editor.md`.

---

# OS Layer

Always the first layer (level 0) of a node.

## Capabilities
- Defines the node name and network id
- Hacking it has no effect

---

# Text Layer

Contains text that is revealed by hacking.

## Capabilities
- Hacking shows the text in the terminal
- text can have formatting and include images

---

# Keystore Layer

Contains the temporary password of a specific ICE layer.

## Capabilities
- Hacking reveals the password, but not which ICE it belongs to
- The password can be entered with the `password` command to bypass the ICE
- The temporary password changes when the site is reset

---

# Status Light / Lock Layer

Represents something in the real world that can be changed (a device that can be turned on, a door that can be unlocked).

## Capabilities
- Hacking opens a page in a new window where the hacker can switch between the options
- Two or more options
- Changes are shown in real time on an external widget page (e.g. a phone or laptop used as a prop)

---

# Daemon Layer

Shows a message when a hacker enters the node.

## Capabilities
- Triggered on arrival, not by hacking. Hacking it has no effect
- Optionally only shown to hackers with a TRAIT skill with the configured key
- Recurrence: FIRST_ENTRY_ONLY, EACH_HACKER_ONCE or EVERY_ENTRY
- A site reset makes it show its message again

---

# Script Credits Layer

Contains data that can be sold for script credits.

## Capabilities
- Hacking transfers the credits to the hacker, after which the layer is marked as stolen
- Requires the SCRIPT_CREDITS skill
- A stolen layer or a layer with amount 0 yields nothing

---

# Script Interaction Layer

Only responds to scripts with the INTERACT_WITH_SCRIPT_LAYER effect and a matching interaction key.

## Capabilities
- Hacking it directly has no effect
- The script shows the message configured on the layer

---

# Network Switch Layer

Lets a hacker choose one of the connections (routes) of its node. The routes that are not chosen are dropped.

## Capabilities
- `hack <layer> <route>` chooses a route, the choice is site-wide
- Until a choice is made, all routes are pending and block movement (jumping to a hacker passes). Dropped routes also block jumps, scripts and scans (the node behind is shown, not scanned)
- Hackers that are cut off from their ingress node (where they connected: the start node, or the target node of an uplink) by a dropped route are disconnected
- Survives a site reset, unlocked by hacking the linked core (can be in another site) or by the GM

---

# Uplink Layer

Connects to another site, usually an internal site that cannot be reached from outside, or a back door into a node behind the front door of a normal site.

## Capabilities
- `view` shows the target site: `1 Uplink -> <site>`
- Optional target node (network id): the ingress node in the target site. Empty: its start node
- `hack <layer>` switches the hacker to the target site. The connection sequence plays with the attack animation (orange connection line) to the ingress node, then the hacker arrives there (arrival triggers layers and scans like an attack does)
- Refused when the target site does not exist, is not hackable, has errors or is shut down, or the target node does not exist
- Each run has one run per target site, so hackers sharing a run also share the run on the target site. The first hacker continues their own run on the target site if they have one, otherwise a new run is created
- Only the hacker that hacks the uplink moves
- Leaving the target site by `dc`, site reset, GM refresh or network switch cut-off returns the hacker to the uplink node, with their original ingress node in that site
- If returning is impossible (site shut down, uplink or node removed, uplink node cut off from the hacker's original ingress node by a dropped route), the hacker falls back to the previous uplink in a chain, or outside the first non-internal site, or home
- Leaving the run (home page, reload) leaves the uplink chain
- Deleting a run also deletes the runs reached via its uplinks, hackers in those runs go home

---

# Internal Site

A site property: the site can only be reached via an Uplink in another site.

## Capabilities
- Starting a run from the home page shows "Site not found"
- Runs on internal sites are not listed on the home page and cannot be entered from a run link
- `/share` is refused for runs on internal sites
