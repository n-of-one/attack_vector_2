# Hacker Skills

Skills define what a hacker can do. They are assigned by GMs and determine which commands, features, and mechanics are available to each hacker.

---

# SCAN

Enables the `scan` command to discover nodes on the network map.

## Capabilities
- Without this skill, the hacker cannot scan sites
- No configurable value — either present or not

---

# SEARCH_SITE

Enables the site search input on the hacker home page.

## Capabilities
- Without this skill, hackers can only join runs shared by others
- No configurable value — either present or not

---

# BYPASS

Allows moving through ICE at the start node.

## Capabilities
- Without this skill, ICE at the entry node blocks all movement
- No configurable value — either present or not

---

# ADJUSTED_SPEED

Modifies the hacker's movement and action speed.

## Capabilities
- Configurable value (integer, minimum 1)
- Affects terminal command processing speed and movement timing
- Default value: 4

---

# STEALTH

Modifies tripwire countdown durations.

## Capabilities
- Configurable percentage value (e.g., +30%, -10%)
- Positive values increase countdown time (more time before shutdown)
- Negative values decrease countdown time (less time)
- Range: -100% to +1000%, cannot be 0
- Applied when a tripwire triggers in a node the hacker enters

---

# SCRIPT_RAM

Enables the script system and determines RAM capacity.

## Capabilities
- Configurable integer value (default: 3)
- Determines how many scripts can be loaded simultaneously
- Without this skill, the script panel is hidden and `run` command is unavailable
- When removed, all loaded scripts are unloaded and script system disabled
- When reduced, excess scripts are unloaded to fit new capacity

---

# SCRIPT_CREDITS

Enables participation in the script marketplace.

## Capabilities
- Configurable integer value representing income amount on payout dates
- Determines credits earned on each income date
- Without this skill, the hacker cannot buy, sell, or transfer scripts
- Default value: 0

---

# WEAKEN

Allows reducing ICE strength by one level.

## Capabilities
- Configurable value: comma-separated list of ICE types (e.g., "word_search, tangle")
- Each listed ICE type gets its own one-time-use weaken ability
- Each weaken can only be used once per site
- Cannot weaken ICE already at "Very Weak" strength
- `weaken` command without arguments shows status of all weaken abilities

---

# UNDO_TRIPWIRE

Enables the `rollback` command.

## Capabilities
- Allows canceling tripwire timers the hacker triggered in the current node
- Moves the hacker back to the previous node after canceling
- No configurable value — either present or not

---

# JUMP_TO_HACKER

Enables the `jump` command.

## Capabilities
- Allows teleporting to another hacker's current node
- Not blocked by ICE during the jump
- No configurable value — either present or not

---

# CREATE_SITE

Allows the hacker to create new sites.

## Capabilities
- Enables site creation capability
- No configurable value — either present or not
