---
sidebar_position: 5
---
# ICE statistics
Attack Vector keeps track of how long it took to hack ICE. These statistics can be downloaded to get a more accurate idea about how long it takes hackers to hack ICE.

Go to the **Statistics** page and click 'Export statistics'. This will download a CSV file.

This is a semicolon separated CSV file with the following columns:

| Column                  | Description                                                                                                                                            |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| ice type                | Type of ICE                                                                                                                                            |
| strength                | Strength                                                                                                                                               |
| state                   | Whether it was hacked or not                                                                                                                           |
| hack time seconds       | The total time from starting to hack, until it was hacked, or the site was reset. If two hackers spent 1 minute to hack this ICE, it will say 60 here. |
| hack time total seconds | The sum of all times from contributing hackers. If two hackers spent 1 minute to hack this ICE, it will say 120 here.                                  |
| hacker count            | The number of hackers that accessed this ICE during the hack.                                                                                          |
| hackers                 | Names of the hackers, separated by \| symbol.                                                                                                          |
| seconds per hacker      | Individual times each hacker contributed, separated by \| symbol.                                                                                      |
| site:node:level         | Reference to the site, node and the layer level                                                                                                        |
| start                   | Start time                                                                                                                                             |
| sweeper resets          | For minesweeper ICE, this is the number of times the players reset it.                                                                                 |
| sweeper mines exploded  | For minesweeper ICE, this is the total number of mines that exploded during all attempts to hack this ICE.                                             |
| iceId                   | The unique ICE ID.                                                                                                                                     |
