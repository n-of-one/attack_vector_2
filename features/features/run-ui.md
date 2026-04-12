# Run - User Interface

The run interface is a split-screen layout that hackers use during an active hacking run. The left panel contains the terminal, scan info, and timers. The right panel shows the site name and the network map canvas.

---

# Terminal Panel

The command-line interface for entering commands during a run.

## Capabilities
- Text input field for typing commands
- Command history navigation with arrow up/down keys
- Clear terminal with Ctrl-L
- Styled output with colored text markup (errors in red, warnings in yellow, info in blue, etc.)
- Terminal locks during operations (scanning, moving) and unlocks when complete
- Prompt indicator changes between outside (">" style) and inside ("⇋" style) contexts

---

# Network Map Canvas

A visual representation of the site's network topology.

## Capabilities
- Displays nodes as icons based on their type (transit, data store, passcode store, ICE, etc.)
- Connections drawn as lines between adjacent nodes
- Hacker positions shown as icons on the map (current user highlighted)
- Undiscovered nodes shown as placeholders
- Node icons update as they are scanned and hacked
- Click a node to view its scan information (replaces terminal with scan info panel)
- Click empty canvas to return to terminal view
- Scan probe animations when scanning
- Movement animations when hackers travel between nodes
- Node appearance animations as they are discovered

---

# Scan Info Panel

Displays detailed information about a selected node. Shown when a node is clicked on the canvas.

## Capabilities
- Shows network ID (or "??" if undiscovered)
- Shows scan progress: 0/3, 1/3, 2/3, 3/3
- At 0/3 (unconnectable): "No information discovered yet"
- At 1/3 (connectable): "No information about layers discovered yet"
- At 2/3 (ICE-protected): shows layers but ICE-shielded ones shown as "unknown (shielded by ICE)"
- At 3/3 (fully scanned): shows all layer details including type, name, and ICE strength/hacked status
- Shows node name if set and scan level is sufficient
- Layer details vary by type (tripwire shows countdown/shutdown info, timer adjuster shows speed/recurrence, core shows reveal-network flag, ICE shows type and strength)

---

# Script Panel

Displays and manages loaded scripts during a run. Only visible if the hacker has the SCRIPT_RAM skill.

## Capabilities
- Collapsible panel (expand/minimize button)
- Shows list of scripts with columns: Code, Name, RAM, State, Action, Effects
- Toggle filter to show only loaded scripts
- RAM usage bar showing loaded / refreshing / free
- Refresh countdown for RAM recovery
- Script credits display (if hacker has SCRIPT_CREDITS skill)
- Sortable by state (loaded first, then available, then used)
- Paginated (26 scripts per page)

---

# Timer Display

Shows active countdown timers above the terminal.

## Capabilities
- Tripwire timers shown in info color: "tripwire [target] → shutdown for HH:MM:SS"
- Shutdown timers shown in warning color
- Live countdown decrement in real-time
- Multiple timers can display simultaneously
- Empty state shows a single placeholder line
