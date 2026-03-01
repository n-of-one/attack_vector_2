# Password ICE

Password ICE protects nodes with a text password. The hacker must know the password in order to 'hack' it.

## Capabilities
- Two UIs: Password ICE (hacker view) and Authentication App (user/app view)
- Correct password hacks the ICE, incorrect password is rejected
- Incremental timeouts between wrong entries, with max timeout.
- Hint shown after 3rd wrong entry
- Accepts both static password (set on the layer) and keystore password
- Password ICE UI shows all wrong entries tried, Authentication App does not
- Duplicate password submissions are silently ignored
- UI shows a countdown timer during timeout lockout

