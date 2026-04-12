# Script Marketplace

The marketplace allows hackers to buy, sell, and trade scripts using script credits. Access requires the SCRIPT_CREDITS skill.

---

# Buying Scripts

Hackers purchase scripts from the marketplace.

## Capabilities
- Marketplace shows all available script types with prices
- Prices are set per-hacker by the GM via script access management
- Scripts with no price set are not shown in the marketplace
- Purchase deducts credits from the hacker's balance
- Purchased script is added to the hacker's inventory in AVAILABLE state
- Credit transaction recorded with description

---

# Script Credits

In-game currency for the script marketplace.

## Capabilities
- Credits stored on the hacker's profile
- Earned via income on scheduled payout dates (amount determined by SCRIPT_CREDITS skill value)
- Spent on purchasing scripts
- Transferable between hackers (both must have SCRIPT_CREDITS skill)
- GM can adjust credits directly
- All transactions are recorded with sender, receiver, amount, and description

---

# Script Income

Periodic credit payouts to hackers.

## Capabilities
- GMs configure income dates as a schedule
- On each income date, hackers can collect credits (one collection per date per hacker)
- Income amount determined by the hacker's SCRIPT_CREDITS skill value
- Income dates have three states: PAST (missed), COLLECTABLE (can collect now), SCHEDULED (future)
- GMs can view which hackers have collected on each date

---

# Credit Transfers

Hackers can send credits to other hackers.

## Capabilities
- Both sender and receiver must have the SCRIPT_CREDITS skill
- Sender's balance must cover the transfer amount
- Recorded as "Manual transfer" in transaction history

---

# Script Offering

Hackers can share scripts with others.

## Capabilities
- Change script state to OFFERING to make it available for download by others
- Withdraw the offer to return script to AVAILABLE state
- Other hackers can download offered scripts using `/download-script` during a run
