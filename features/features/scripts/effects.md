# Script Effects

Each script type can have multiple effects that trigger when the script is executed. Effects are either positive (helping the hacker) or negative (drawbacks/penalties). Effects execute in sequence.

---

# AUTO_HACK_ANY_ICE

Automatically hacks any ICE layer.

## Capabilities
- Instantly hacks ICE 
- Works on any ICE type regardless of strength

---

# AUTO_HACK_ICE_TYPE

Automatically hacks ICE of a specific type.

## Capabilities
- Configured with a single ICE type
- Only works on the specified ICE type

---

# AUTO_HACK_ICE_BY_STRENGTH

Automatically hacks ICE at or below a strength threshold.

## Capabilities
- Configured with a strength level
- Configured with excluded ICE types
	- Default exclusions are: PASSWORD_ICE, TAR_ICE. Can be empty


---

# AUTO_HACK_SPECIFIC_ICE_LAYER

Automatically hacks a pre-defined specific ICE layer.

## Capabilities
- Configured with a specific layer ID
- Only works on that exact layer
- Fails if the layer doesn't exist, isn't ICE, or the ID has changed

---

# HACK_BELOW_NON_HACKED_ICE

Hacks a layer that is shielded by unhacked ICE above it.

## Capabilities
- Bypasses the normal requirement to hack ICE layers in order

---

# ROTATE_ICE

Changes an ICE layer to a different ICE type.

## Capabilities
- Rotates through: Word Search → Tangle → Netwalk → Sweeper → Word Search
- Generates a new puzzle of the new type

---

# SWEEPER_UNBLOCK

Unblocks a hacker who hit a mine in Sweeper ICE.

---

# TANGLE_REVEAL_CLUSTERS

Reveals the cluster groupings in a Tangle ICE puzzle.

## Capabilities
- Shows which points belong to which cluster

---

# WORD_SEARCH_NEXT_WORDS

Reveals upcoming words in a Word Search ICE puzzle.

## Capabilities
- Configured with a number of words to reveal, default: 5

---

# JUMP_TO_NODE

Teleports the hacker to a different node in the site.

## Capabilities
- Configurable block mode: BLOCKED_BY_ICE (default) or NOT_BLOCKED_BY_ICE

---

# JUMP_TO_HACKER

Teleports to another hacker's current node.

## Capabilities
- Configurable block mode: BLOCKED_BY_ICE or NOT_BLOCKED_BY_ICE

---

# SCAN_ICE_NODE

Scans nodes beyond an ICE-protected node.

## Capabilities
- Targets a specific node (network ID)
- Target node must have ICE

---

# DELAY_TRIPWIRE_COUNTDOWN

Adds time to an active tripwire countdown.

## Capabilities
- Configured with a duration to add (e.g., "00:01:00" for 1 minute)

---

# START_RESET_TIMER

Starts a site-wide reset countdown (negative/drawback effect).

## Capabilities
- Configured with a countdown duration (default 15 minutes)
- Only one reset timer can be active per site
	- if there already is one, this effect does nothing
- When countdown reaches zero, site enters a 2-minute shutdown

---

# SPEED_UP_RESET_TIMER

Accelerates an existing reset countdown (negative/drawback effect).

## Capabilities
- Configured with a duration to subtract from the countdown
- Only has an effect if a reset timer is already active, otherwise does nothing

---

# DECREASE_FUTURE_TIMERS

Reduces the duration of all future tripwire/timer events (negative/drawback effect).

## Capabilities
- Configured with a duration reduction (e.g., "00:01:00")
- Affects all timers created after this effect runs
- Global effect on the site, resets when the site resets.

---

# SHOW_MESSAGE

Displays a custom text message to the hacker.

## Capabilities
- Configured with a message string
- Informational only — no gameplay effect
- Shown in the terminal

---

# SITE_STATS

Displays site statistics that is useful as  reconnaissance data.

## Capabilities
- Shows: node count, core count, tripwire count, ICE type counts with max strength 

---

# INTERACT_WITH_SCRIPT_LAYER

Triggers a custom interaction on a ScriptInteraction layer.

## Capabilities
- Configured with an interaction key
- Target layer must be a ScriptInteraction layer with a matching key
- Effect text is stored in the ScriptInteractio

---

# HIDDEN_EFFECTS

Hides all effects to the right of this one from the hacker's view.

## Capabilities
- Effects after this one in the list are shown as "Unknown effect" to hackers
