# Hacker Home

The home page for hackers after logging in. Provides access to available runs and the ability to start new ones.

---

# Site Search

Allows hackers to find and start runs against sites.

## Capabilities
- Text input to type a site name
- Search button to initiate a new run
- Only visible if the hacker has the SEARCH_SITE skill
- Without the skill, a message instructs the hacker to join runs shared by others

---

# Active Runs Table

Lists all runs the hacker has access to.

## Capabilities
- Columns: Site name, Nodes count, Actions
- Click site name to enter the run
- Delete button (X icon) removes the run link — only shown if HACKER_DELETE_RUN_LINKS config is enabled
- Reset button (refresh icon) resets ICE and timers for the site — only shown if DEV_HACKER_RESET_SITE config is enabled, with confirmation dialog

---

# Run Entry Flow

The sequence when a hacker enters an existing run.

## Capabilities
- Click on a run in the table to prepare and enter
- Two-step process: prepare to enter (server sets up hacker state), then enter (WebSocket connection tracked)
- After entering, the run UI loads with terminal, canvas, and timers

---

# User Preferences

Hacker-configurable settings.

## Capabilities
- Font size adjustment for the terminal
- Character name editing (if HACKER_EDIT_CHARACTER_NAME config is enabled)
- Username editing (if HACKER_EDIT_USER_NAME config is enabled)
