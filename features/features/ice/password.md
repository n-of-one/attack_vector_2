# Password ICE

Password ICE protects nodes with a text password. The hacker must know the password in order to 'hack' it.

## Capabilities
- Two UIs: Password ICE (hacker view) and Authentication App (user/app view)
- Correct password hacks the ICE, incorrect password is rejected
- Incremental timeouts between wrong entries (2s for first 3 attempts, 5s for attempts 4-6, 10s for attempts 7-11, 15s after that)
- Hint shown after 3rd wrong entry (if hint text is configured on the layer)
- Accepts both static password (set on the layer) and keystore password (retrieved from keystore system)
- Password ICE UI shows all wrong entries tried, Authentication App does not
- Duplicate password submissions are silently ignored
- UI shows a countdown timer during timeout lockout
- Hacked state is tracked per-user via an authorization list
