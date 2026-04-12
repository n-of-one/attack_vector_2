# Site Editor

The visual editor for designing sites. GMs use the editor to create sites for hackers to hack.

---

# Canvas, nodes and connections

The main editing surface for placing and connecting nodes.

## Capabilities
- Visual canvas displaying nodes and connections
- Add and remove nodes
- Reposition nodes
- Add and remove connections
- Option to snap all nodes to grid alignment
- Option to center all nodes on the canvas

---

# Node Management

Managing layers within a node and node properties via OS layer.

## Capabilities
- Always has OS layer that defines node name and network id
- Add additional layers
- Remove additional layers
- Change order of additional layers


---

# Layer Types and Properties

Each layer type has configurable properties.

## Capabilities
- **OS**: name, note
- **TEXT**: name, note, text
- **KEYSTORE**: name, note, target ice id
- **TRIPWIRE**: name, note, countdown duration, shutdown duration, linked core layer, linked core site
- **TIMER_ADJUSTER**: name, note, adjustment amount, type (SPEED_UP / SLOW_DOWN), recurrence (FIRST_ENTRY_ONLY / EVERY_ENTRY / EACH_HACKER_ONCE)
- **CORE**: name, note, reveal-network flag
- **STATUS_LIGHT**: name, note, multiple display options (color, label, icon)
- **LOCK**: same as **STATUS_LIGHT**
- **SCRIPT_INTERACTION**: name, note, interaction key
- **SCRIPT_CREDITS**: name, note, credit amount, stolen status
- **PASSWORD_ICE**: name, note, password, hint
- **TANGLE_ICE**: name, note, strength
- **WORD_SEARCH_ICE**: name, note, strength
- **NETWALK_ICE**: name, note, strength
- **TAR_ICE**: name, note, strength
- **SWEEPER_ICE**: name, note, strength

---

# Site Properties

Global settings for the site.

## Capabilities
- Name
- Purpose/plot
- Owner
- Description
- Start node network ID
- Nodes locked toggle
- Hackable (not shown or editable in editor)

---

# Site Validation

Automatic checks for site integrity.

## Capabilities
- Info and error messages
- error: service name empty (all layers)
- info: site name too long to be displayed nicely
- info: site purpose not filled in
- error: start node network id not set
- error: start node network id not found
- error: node not reachable from start node
- error: os network id empty
- error: os network id not unique
- error: text hacked text empty
- error: keystore not linked to ICE layer
- error: password ICE has empty password
- error: tripwire countdown duration invalid
- error: tripwire shutdown duration invalid
- error: tripwire shutdown must be at least minimum duration
- info: tripwire not connected to core
- error: tripwire connected to removed core layer
- error: tripwire connected to non-core layer
- error: timer adjuster amount duration invalid
- error: scriptcredits amount negative
- info: scriptcredits amount not yet set
- info: scriptcredits stolen
- error: scriptinteraction interaction key empty
- info: scriptinteraction message empty

