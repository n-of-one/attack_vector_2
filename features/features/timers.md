# Timers & Site Shutdown

Timers create time pressure during hacking runs. They are triggered by tripwire layers, modified by timer adjuster layers, and stopped by core layers.

---

# Tripwire Layer

Starts a countdown when a hacker enters the node.

## Capabilities
- Configurable countdown duration (e.g., 15:00 = 15 minutes)
- Configurable shutdown duration (how long site stays offline after countdown reaches zero)
- Countdown adjusted by site alertness property (GM-configurable offset)
- Countdown adjusted by hacker's STEALTH skill (percentage reduction)
- Minimum enforced duration of 10 seconds (prevents instant shutdown)
- Can link to a core layer in the same site or a different site (triggers shutdown on the linked site)
- Each tripwire can only trigger its timer once per run
- Hacker sees terminal message with countdown time remaining

---

# Timer Adjuster Layer

Modifies active tripwire countdowns when a hacker enters the node.

## Capabilities
- Type: SPEED_UP (subtracts time) or SLOW_DOWN (adds time)
- Amount: configurable duration adjustment (e.g., 5:00)
- Three recurrence modes:
  - FIRST_ENTRY_ONLY: only the first hacker to enter triggers it
  - EVERY_ENTRY: every hacker entry triggers the adjustment
  - EACH_HACKER_ONCE: each unique hacker triggers it once
- Only affects tripwire countdown timers (not script-created timers)
- Terminal message confirms the adjustment applied

---

# Core Layer

Stops tripwire timers when hacked.

## Capabilities
- Linked from one or more tripwire layers
- Hacking the core cancels all tripwire timers linked to it
- Can receive links from tripwires in other sites (cross-site timer control)
- Cannot be deleted if tripwires in other sites reference it
- Optionally reveals the full network map when hacked

---

# Site Shutdown Sequence

What happens when a tripwire countdown reaches zero.

## Capabilities
- Site marked as in shutdown mode with an end timestamp
- All hackers in the site receive a notification
- New connection attempts are rejected with "connection refused" / "site is in shutdown mode"
- After the shutdown duration elapses, the site becomes hackable again
- Hackers can reconnect after shutdown ends
- Minimum shutdown duration configurable by admin (default 10 seconds in test mode)

---

# Multiple Timers

Several timers can run simultaneously.

## Capabilities
- Multiple tripwire layers can trigger independent countdowns
- Script effects (START_RESET_TIMER, SPEED_UP_RESET_TIMER) create separate timer tracks
- Timer adjusters only affect tripwire-origin timers, not script-created timers
- All active timers displayed in the timer panel above the terminal
- On server restart: countdown timers are cleared, shutdown timers complete immediately
