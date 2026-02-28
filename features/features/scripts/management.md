# Script Management

Scripts are one-time-use items that provide tactical advantages during hacking runs. GMs create script types with effects, distribute them to hackers, and hackers manage them in RAM.

---

# Script Type Management (GM)

GMs define the templates for scripts including their effects and properties.

## Capabilities
- Create new script types with name, category, RAM size, and GM notes
- Add positive (useful) or negative (drawback) effects to a script type
- Configure effect parameters (e.g., target layer, duration, ICE type)
- Edit script type properties: name (1-15 chars), category, size, default price, GM notes
- Delete script types (cascades to delete all instances and access rules)
- View player-facing effect descriptions as they will appear to hackers

---

# Script Access Management (GM)

Controls which hackers can acquire which scripts.

## Capabilities
- Set per-hacker prices for each script type (overrides default price)
- Price of null means not available to that hacker
- Manage access across all hacker accounts

---

# Script Distribution (GM)

GMs can directly give scripts to hackers.

## Capabilities
- Add script instances to a hacker's inventory
- Load scripts into a hacker's RAM directly
- View a hacker's current scripts and their states
- Delete scripts from a hacker's inventory

---

# Hacker Script Management

Hackers manage their personal script inventory.

## Capabilities
- View all owned scripts with their states: AVAILABLE, LOADED, USED, EXPIRED, OFFERING
- Load scripts from inventory into RAM (consumes RAM capacity)
- Unload scripts from RAM back to inventory (RAM enters refresh cycle)
- Delete used/expired scripts (cleanup)
- View RAM status: loaded / refreshing / free
- Scripts expire at 06:00 the next day after being distributed
- Requires the SCRIPT_RAM skill to access

---

# Script RAM System

RAM limits how many scripts a hacker can have active.

## Capabilities
- Total RAM capacity determined by SCRIPT_RAM skill value
- Loading a script consumes RAM equal to the script type's size
- Unloading a script puts that RAM into a refresh cycle (gradually becomes free again)
- Refresh duration configurable by admin
- RAM is locked during and shortly after a run (lockout duration configurable)
- If SCRIPT_RAM skill is removed, all scripts are unloaded and RAM disabled

---

# Script Execution

Running a script during a hacking run.

## Capabilities
- Terminal command: `run <script-code>` with optional arguments
- Script must be in LOADED state and not expired
- All effects execute in sequence
- Script is marked USED after execution (cannot be reused)
- RAM freed after use (enters refresh cycle)
- Some effects may keep the terminal locked during execution
