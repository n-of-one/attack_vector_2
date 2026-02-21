---
sidebar_position: 1
---
# Layers overview
A site is made up of nodes that each contain one or more layers. There are different types of layers, and what happens if you hack a layer depends on the layer type.

Layers also have a name that indicates what the layer is. This name is independent from the layer type. For example:

```
⇋ view 
Node service layers: 
0 OS 
1 Database 
```

You see that the current node has an **OS** layer at level 0, which is always going to be of the type OS.

But what type the **database** layer is, you don't know yet. The only way to find out is by hacking the layer with the `hack` command.
