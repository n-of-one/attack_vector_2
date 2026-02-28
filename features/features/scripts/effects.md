# Script Effects

Each script type can have multiple effects that trigger when the script is executed. Effects are either positive (helping the hacker) or negative (drawbacks/penalties). Effects execute in sequence.

---

# AUTO_HACK_ANY_ICE

Automatically hacks any ICE layer.

## Capabilities
- Requires a layer ID argument when running the script
- Instantly completes the ICE puzzle with a 5-second animation
- Works on any ICE type regardless of strength

---

# AUTO_HACK_ICE_TYPE

Automatically hacks ICE of a specific type.

## Capabilities
- Configured with an ICE type (e.g., WORD_SEARCH_ICE, TANGLE_ICE)
- Only works on the specified ICE type
- Fails if the target layer is a different ICE type

---

# AUTO_HACK_ICE_BY_STRENGTH

Automatically hacks ICE at or below a strength threshold.

## Capabilities
- Configured with a strength level and optional excluded ICE types (e.g., "WEAK:PASSWORD_ICE,TAR_ICE")
- Only hacks ICE with strength at or below the threshold
- Excluded ICE types are never hacked by this effect
- Fails if the ICE is too strong or is an excluded type

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
- Requires a layer ID argument
- Bypasses the normal requirement to hack ICE layers in order
- Only works if there is at least one unhacked ICE layer above the target

---

# ROTATE_ICE

Changes an ICE layer to a different ICE type.

## Capabilities
- Rotates through: Word Search → Tangle → Netwalk → Sweeper → Word Search
- Only works on ICE that hasn't been hacked yet
- Generates a new puzzle of the new type

---

# SWEEPER_UNBLOCK

Unblocks a hacker who hit a mine in Sweeper ICE.

## Capabilities
- Requires a Sweeper ICE layer ID argument
- Removes the hacker from the blocked list
- Only works on Sweeper ICE layers

---

# TANGLE_REVEAL_CLUSTERS

Reveals the cluster groupings in a Tangle ICE puzzle.

## Capabilities
- Requires a Tangle ICE layer ID argument
- Shows which points belong to which cluster
- Only works on Tangle ICE layers

---

# WORD_SEARCH_NEXT_WORDS

Reveals upcoming words in a Word Search ICE puzzle.

## Capabilities
- Configured with a number of words to reveal
- Shows the next N words the hacker needs to find
- Only works on Word Search ICE layers

---

# JUMP_TO_NODE

Teleports the hacker to a different node in the site.

## Capabilities
- Requires a network ID argument when running the script
- Configurable block mode: BLOCKED_BY_ICE (default) or NOT_BLOCKED_BY_ICE
- Target node must be discovered (scanned)
- If blocked mode: fails if ICE blocks the path

---

# JUMP_TO_HACKER

Teleports to another hacker's current node.

## Capabilities
- Requires a username argument when running the script
- Configurable block mode: BLOCKED_BY_ICE or NOT_BLOCKED_BY_ICE
- Target hacker must be in the same run and inside the site

---

# SCAN_ICE_NODE

Scans nodes beyond an ICE-protected node.

## Capabilities
- Requires a network ID argument
- Reveals nodes that are normally hidden behind ICE barriers
- Target node must have ICE

---

# DELAY_TRIPWIRE_COUNTDOWN

Adds time to an active tripwire countdown.

## Capabilities
- Configured with a duration to add (e.g., "00:01:00" for 1 minute)
- Only works if there is an active tripwire countdown in the current node

---

# START_RESET_TIMER

Starts a site-wide reset countdown (negative/drawback effect).

## Capabilities
- Configured with a countdown duration (default 15 minutes)
- Only one reset timer can be active per site
- When countdown reaches zero, site enters a 2-minute shutdown

---

# SPEED_UP_RESET_TIMER

Accelerates an existing reset countdown (negative/drawback effect).

## Capabilities
- Configured with a duration to subtract from the countdown
- Only works if a reset timer is already active

---

# DECREASE_FUTURE_TIMERS

Reduces the duration of all future tripwire/timer events (negative/drawback effect).

## Capabilities
- Configured with a duration reduction (e.g., "00:01:00")
- Affects all timers created after this effect runs
- Global effect on the site

---

# SHOW_MESSAGE

Displays a custom text message to the hacker.

## Capabilities
- Configured with a message string
- Informational only — no gameplay effect
- Shown in the terminal

---

# SITE_STATS

Displays comprehensive site statistics.

## Capabilities
- Shows: node count, core count, tripwire count, ICE type counts with max strengths
- Provides reconnaissance data about the site
- No arguments required

---

# INTERACT_WITH_SCRIPT_LAYER

Triggers a custom interaction on a ScriptInteraction layer.

## Capabilities
- Configured with an interaction key
- Requires a layer ID argument
- Target layer must be a ScriptInteraction layer with a matching key

---

# HIDDEN_EFFECTS

Hides all effects to the right of this one from the hacker's view.

## Capabilities
- Effects after this one in the list are shown as "Unknown effect" to hackers
- Creates uncertainty about what the script will actually do
- GM can still see all effects
