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
- Hackers that are cut off from the start node by a dropped route are disconnected
- Survives a site reset, unlocked by hacking the linked core (can be in another site) or by the GM
