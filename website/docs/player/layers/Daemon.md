---
sidebar_position: 12
---
# Daemon
This layer shows a message when a hacker enters the node.

```
⇋ move 04
Entered node 04
Connection logged. Welcome back, operator.
```

A daemon can be set up to only show its message to hackers that have a specific trait (a **Trait** skill). Other hackers will not notice anything when they enter the node.

When the same or a different hacker enters the node (again), whether the message is shown depends on how the layer is configured. Options are:
- The message is shown only once, to the first hacker that enters the node
- The message is shown once to every hacker
- The message is shown every time a hacker enters the node

If the site is reset, the daemon will show its message again on new entry.

## Hacking
Hacking a daemon has no effect.

```
⇋ hack 1
Hacking this layer reveals nothing new.
```
