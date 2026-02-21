# Tangle ICE
This ICE is called Gaanth in game. Gaanth is a rough rendition of the Hindi word for `knot`.

![Picture of a tangle ICE puzzle](../../assets/players/ice/tangle.png)  

You are you are breaking a weak encryption scheme that is represented as tangled lines.

## Minigame
Tangle ICE asks players to untangle a mess of lines so that none of them are crossing. Drag the connection points around with your mouse.

This is a version of [Simon Tatham's Untangle puzzle](https://www.chiark.greenend.org.uk/~sgtatham/puzzles/js/untangle.html).

## ICE strength

If the ICE strength is weak, then there are fewer lines to untangle.

If the ICE strength is strong, then there are more lines to untangle. There will also be more clusters.

## Clusters

Tangle ICE can have 2 or more clusters. Each cluster is a separate set of connected lines. Clusters can be untangled separately.

![A picture of a tangle ICE puzzle with 2 clusters](http://localhost:3000/img/website/players/ice-tangle-clusters.png)  

| Strength    | Clusters |
| ----------- | -------- |
| Very weak   | 1        |
| Weak        | 2        |
| Average     | 3        |
| Strong      | 3        |
| Very strong | 4        |
| Onyx        | 4        |


## Multiplayer
Multiple hackers can work on the same puzzle at the same time. This introduces coordination problems, as players can undo each other's work.

In practice, it's only useful for one or two players to work on the same cluster at the same time. More players will just be in each other's way.

So, if Tangle ICE has 2 or more clusters, then it becomes more useful for multiple players to cooperate to solve it.
