# TAR ICE

TAR ICE simulates a time-based hacking process. Instead of solving a puzzle, the hacker waits while a simulated hacker crew progressively cracks the ICE.

## Capabilities
- Progress bar showing units hacked out of total units required
- Hacker crew of simulated team members contributes units per second based on their levels
- Total units scale with ICE strength (9,000 at Very Weak / ~10 minutes, up to 7,776,000 at Onyx / ~144 hours)
- Each simulated hacker level contributes 10 + level units/second
- Frontend simulates progression and reports to server
- Server validates that units can only increase (anti-cheat)
- Win condition: units hacked >= total units
- Time estimates shown for different crew sizes (1 level-1 hacker, 1 level-5 hacker, 5 level-10 hackers)
- New TAR instance with reset progress on reset
