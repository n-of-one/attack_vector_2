---
sidebar_position: 7
---
# Keystore
This layer contains a temporary password of a specific ICE layer. Hacking this layer will reveal the password, but not what ICE it is for.

```
⇋ view 
Node service layers: 
0 OS
1 Keystore 

⇋ hack 1 
hacked. Password found: Gaanth/7n-43-6f-3a/3u-54-4u-1t/
```

This temporary password can be used to bypass a specific ICE layer without having to complete the mini-game. The first part of the password indicates the type of ICE this password is for. In this example 'Gaanth' is the in-game name for the Tangle ICE. You can open the password interface of an ICE layer with the `password` terminal command.

The temporary password of the ICE will change if the site is reset. 

Rahasy or 'Password' ICE layers have a permanent password that does not change when the site is reset. This permanent password not stored in a keystore layer, but must be found somewhere else.