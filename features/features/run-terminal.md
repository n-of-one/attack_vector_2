# Run - Terminal Commands

The terminal is the primary interface for hackers during a run. Commands are context-sensitive — some only work outside the site (before entering), some only inside, and some work in both contexts.

---

# scan

Scans the site from outside, revealing nodes on the network map.

## Capabilities
- Requires the SCAN skill
- Reveals nodes up to but not past ICE-protected nodes
- Locks the terminal during the scanning animation
- Ignores any arguments (with a warning)
- Fails if the site is in shutdown mode

---

# attack

Enters the site and starts the hacking run from the designated start node.

## Capabilities
- Only works from outside the site
- Timed entry sequence with animation
- Terminal prompt changes to indicate "inside" context
- Initializes script RAM for the run
- Hacker arrives at the start node
- Fails if the site is in shutdown mode

---

# qs (quick scan)

Development-only fast scan command.

## Capabilities
- Only available when dev commands are enabled
- Same effect as scan but skips the animation timing

---

# qa (quick attack)

Development-only fast attack command.

## Capabilities
- Only available when dev commands are enabled
- Same effect as attack but with fast entry timing

---

# view

Displays the layers in the current node.

## Capabilities
- Lists all layers with their level number, name, and type
- Shows ICE hacked status for ICE layers
- Layers below unhacked ICE are shown as "unknown (shielded by ICE)"
- Shows node name if one is set
- Only works from inside the site

---

# move

Moves the hacker to an adjacent connected node.

## Capabilities
- Requires a network ID argument (e.g., `move 01`)
- Movement has timed animation (affected by skills and config)
- Auto-scans unscanned nodes on arrival
- Triggers tripwire and timer adjuster layers on arrival
- Can always move back to the previous node regardless of ICE blocking
- Blocked by unhacked ICE in the current node (unless moving backward)
- BYPASS skill allows moving through ICE at the start node
- Error: node not found, already at that node, no path, blocked by ICE

---

# hack

Hacks a layer in the current node.

## Capabilities
- Requires a layer number argument (e.g., `hack 0`)
- ICE layers open an interactive puzzle in a popup window
- Non-ICE layers (OS, Text, Keystore, Core, Tripwire, etc.) are hacked directly
- Layers below unhacked ICE are blocked — must hack ICE first
- Already-hacked ICE layers report "not required"
- Only works from inside the site

---

# qhack (quick hack)

Development-only instant hack for ICE layers.

## Capabilities
- Only available when dev commands are enabled
- Instantly hacks ICE without opening the puzzle interface
- Only works on ICE layers (non-ICE layers rejected)

---

# password

Opens the password interface for an ICE layer.

## Capabilities
- Requires a layer number argument
- Opens ICE password connection UI in a popup window
- Only works on ICE layers
- Blocked by unhacked ICE above the target layer

---

# qmove (quick move)

Development-only instant move command.

## Capabilities
- Only available when dev commands are enabled
- Same validation as move but skips all timing/animation

---

# weaken

Reduces the strength of an ICE layer by one level.

## Capabilities
- Requires the WEAKEN skill (one per ICE type: tangle, word_search, netwalk, tar, sweeper)
- Without arguments: shows status of all weaken skills and which sites they've been used on
- With layer number argument: weakens that ICE layer
- Each weaken skill can only be used once per site
- Cannot weaken ICE already at "Very Weak" strength
- Skill must match the ICE type (e.g., tangle weaken only works on Tangle ICE)

---

# rollback

Cancels active tripwire timers and moves back to the previous node.

## Capabilities
- Requires the UNDO_TRIPWIRE skill
- Stops all timers triggered by the current hacker in the current node
- Immediately moves back to the previous node
- Does not trigger layers at the destination node
- Fails if there is no previous node, no tripwires, or no timers triggered by this hacker

---

# jump

Teleports to another hacker's current node.

## Capabilities
- Requires the JUMP_TO_HACKER skill
- Requires a username argument
- Teleports instantly without being blocked by ICE
- Target hacker must be in the same run and inside the site
- Triggers tripwire and timer adjuster layers at the destination
- Fails if target user not found, not in run, outside the site, or at the same node

---

# dc (disconnect)

Disconnects from the site and returns to outside state.

## Capabilities
- Returns the hacker to the run's outside context
- Clears inside-specific syntax highlighting

---

# run

Executes a loaded script.

## Capabilities
- Requires the SCRIPT_RAM skill
- Takes a script code argument and optional script-specific arguments
- Validates the script is loaded in RAM and not expired
- Executes all script effects in sequence
- Marks the script as used after execution (one-time use)
- Works from both inside and outside the site

---

# /download-script

Downloads a script to RAM during a run.

## Capabilities
- Requires HACKER_SCRIPT_LOAD_DURING_RUN config to be enabled
- Requires the SCRIPT_RAM skill
- Takes a script code argument
- Downloads the script via the script service

---

# /share

Shares the current run with other hackers.

## Capabilities
- Takes one or more usernames (space or comma separated)
- Creates run links so specified hackers can join the run
- Fails if a user is not found
- Works from both inside and outside the site

---

# help

Displays context-sensitive help.

## Capabilities
- Outside context: shows scan, attack, run, /share, /download-script commands
- Inside context: shows view, move, hack, scan, run, weaken, rollback, jump, password, dc commands
- `help shortcuts` shows keyboard shortcuts (arrow up/down for history, Ctrl-L to clear)
- Dev commands shown when dev mode is enabled

---

# sweeperunblock

Development-only debug command to unblock a hacker from Sweeper ICE.

## Capabilities
- Only available when dev commands are enabled
- Requires a layer number argument
- Only works on Sweeper ICE layers

---

# quick

Development-only command to enable quick playing mode.

## Capabilities
- Only available when dev commands are enabled
- Enables quick ICE playing mode (bypasses timing/animations for faster testing)
- Toggles the quick playing configuration setting
