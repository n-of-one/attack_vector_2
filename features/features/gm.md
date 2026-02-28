# GM Management

The Game Master (GM) manages sites, users, scripts, and monitors game activity. The GM page is the primary interface for running a LARP event.

---

# Site Management

Creating, configuring, and controlling hackable sites.

## Capabilities
- Create new site by entering a name (initializes with a default transit node)
- Delete site (blocked if core layers are linked from tripwires in other sites)
- Copy site (duplicates all nodes, connections, layers, properties with auto-incremented name)
- Toggle site hackable status (cannot enable if site has validation errors)
- Reset site (clears all in-progress runs, resets ICE and timers)
- Delete all runs for a site
- Import site from JSON file
- Export site to JSON file
- Site list shows name, hackable status, and validation status (ok or error details)

---

# User Management

Creating and managing hacker accounts.

## Capabilities
- Create user by entering a username
- Edit user fields: name, type (HACKER / HACKER_MANAGER / GM / ADMIN), tag (REGULAR / DEVELOPER / ORGANIZER), character name
- Delete user permanently
- View user details and associated hacker profile
- Add, edit, and remove skills for each user
- Manage skill values (e.g., RAM amount, speed, stealth percentage, weaken types)

---

# Script Management

Covered in detail in `scripts/management.md`. From the GM perspective:

## Capabilities
- Create and configure script types with effects
- Manage script access per hacker (set prices)
- Distribute scripts directly to hackers
- View hacker script inventories
- Configure script income dates and payout schedules
- Manage hacker credit balances

---

# Statistics

Viewing game performance data.

## Capabilities
- Export ICE hack statistics as CSV (semicolon delimiter)
- Data includes hack duration per ICE type and strength combination
- Tracks which hackers completed which ICE challenges

---

# Task Monitor

Real-time view of active backend operations.

## Capabilities
- Shows all active scheduled tasks
- Columns: due time, action name, user, task identifiers (siteId, layerId, runId)
- Useful for debugging timer and script execution issues
