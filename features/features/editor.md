# Site Editor

The visual editor for designing site network topologies. GMs use the editor to create and configure the maps that hackers will attack.

---

# Canvas

The main editing surface for placing and connecting nodes.

## Capabilities
- Visual canvas displaying nodes and connections
- Click and drag nodes to reposition them
- Real-time updates for all editors viewing the same site
- Snap all nodes to grid alignment
- Center all nodes on the canvas

---

# Node Management

Adding, removing, and configuring network nodes.

## Capabilities
- Add new nodes (default type: TRANSIT_2, also APP type available)
- Move nodes by dragging on canvas
- Delete nodes (removes all associated connections, validates site integrity)
- Edit network ID for each node (used for navigation commands like `move 01`)
- Each node can have multiple stacked layers

---

# Connection Management

Defining the paths between nodes.

## Capabilities
- Add connections between two nodes
- Delete individual connections or all connections from a node
- Cannot create duplicate connections between the same node pair
- Connections define which moves are valid during a run

---

# Layer Management

Configuring the security and content layers on each node.

## Capabilities
- Add layers from a type dropdown (16 types available)
- Remove layers (with validation — core layers linked by external tripwires cannot be removed)
- Swap layer order (move up/down) — affects hack order during runs
- Edit layer-specific properties

---

# Layer Types and Properties

Each layer type has configurable properties.

## Capabilities
- **OS**: name, note
- **TEXT**: name, note, free-form text content
- **KEYSTORE**: name, note, stored passwords (used by Password ICE)
- **LOCK**: name, note (prevents further hacking below)
- **TRIPWIRE**: name, note, countdown duration, shutdown duration, linked core layer (same or different site)
- **TIMER_ADJUSTER**: name, note, adjustment amount, type (SPEED_UP / SLOW_DOWN), recurrence (FIRST_ENTRY_ONLY / EVERY_ENTRY / EACH_HACKER_ONCE)
- **CORE**: name, note, reveal-network flag (shows full map when hacked)
- **STATUS_LIGHT**: name, note, multiple display options (color, label, icon)
- **SCRIPT_INTERACTION**: name, note, interaction key
- **SCRIPT_CREDITS**: name, note, credit amount (GM-only layer)
- **PASSWORD_ICE**: name, note, strength, password, hint text
- **TANGLE_ICE**: name, note, strength
- **WORD_SEARCH_ICE**: name, note, strength
- **NETWALK_ICE**: name, note, strength
- **TAR_ICE**: name, note, strength
- **SWEEPER_ICE**: name, note, strength

---

# Site Properties

Global settings for the site.

## Capabilities
- Name (unique identifier)
- Description (free-form text)
- Purpose/plot (story context)
- Start node (network ID of the entry point)
- Hackable toggle (must be true for hackers to start runs)
- Nodes locked toggle (prevents hackers from moving on the map)

---

# Site Validation

Automatic checks for site integrity.

## Capabilities
- All tripwires must link to valid core layers
- All nodes must be reachable from the start node
- Shutdown duration must be at least 1 second
- Validation status shown in GM site list as "ok" or error details
- Cannot make a site hackable if it has validation errors
