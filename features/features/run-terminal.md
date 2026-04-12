# Run - Terminal Commands

The terminal is the primary interface for hackers during a run. Commands are context-sensitive — some only work outside the site (before entering), some only inside, and some work in both contexts.

---

# scan

Scans the site from outside, revealing nodes on the network map.

## Capabilities
- Requires the SCAN skill
- Reveals nodes up to but not past ICE-protected nodes
- works outside and inside


---

# attack

Enters the site and starts the hacking run from the designated start node.

## Capabilities
- Transition to inside the run
- Auto scan if start node is not scanned


---

# view

Displays the layers in the current node.

## Capabilities
- Lists all layers with their level number, name, and type
- Shows ICE hacked status for ICE layers
- Layers below unhacked ICE are shown as "unknown (shielded by ICE)"


---

# move

Moves the hacker to an adjacent connected node.

## Capabilities
- Movement animation duration depends on hacker speed
- Auto-scans unscanned nodes on arrival
- Triggers some layers on arrival
	- tripwire
	- timer adjuster layers
- Blocked by unhacked ICE in the current node (unless moving backward)
- BYPASS skill allows moving through ICE at the start node

---

# hack

Hacks a layer in the current node.

## Capabilities
- ICE layers open an interactive puzzle in a popup window
- Non-ICE layers (OS, Text, Keystore, Core, Tripwire, etc.) are hacked directly


---

# password

Opens the password interface for an ICE layer.

## Capabilities
- Opens ICE password connection UI in a popup window
- Works on all  ICE layers

---

# weaken

Reduces the strength of an ICE layer by one level.

## Capabilities
- Requires the WEAKEN skill
	- Skill value must match the ICE type
- Each weaken skill can only be used once per site until it is reset

---

# rollback

Cancels active tripwire timers and moves back to the previous node.

## Capabilities
- Requires the UNDO_TRIPWIRE skill
- Stops all timers triggered by the current hacker in the current node
- Immediately moves back to the previous node


---

# jump

Teleports to another hacker's current node.

## Capabilities
- Requires the JUMP_TO_HACKER skill
- Teleports instantly without being blocked by ICE

---

# dc (disconnect)

Disconnects from the site and returns to outside state.

## Capabilities
- Returns the hacker to the run's outside context

---

# run

Executes a loaded script.

## Capabilities
- Requires the SCRIPT_RAM skill
- Executes script

---

# /download-script

Downloads a script offered by some else, similar to how it can be done in the Hacker's Scripts page

## Capabilities
- Requires HACKER_SCRIPT_LOAD_DURING_RUN config to be enabled
- Requires the SCRIPT_RAM skill
- Downloads the script

---

# /share

Shares the current run with other hackers.

## Capabilities
- Creates run links so specified hackers can join the run

---

# help

Displays context-sensitive help.

## Capabilities
- Show commands that are available in the current context (outside, inside) 
- `help shortcuts` shows keyboard shortcuts
