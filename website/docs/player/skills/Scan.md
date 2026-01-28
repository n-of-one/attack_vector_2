# Scan
This skill allows you to use the `scan` command to perform a scan of the system, revealing nodes up to and including the nodes that contain ICE. The scan won't reveal nodes beyond nodes containing ICE.

If you don't have access to this command, you can still scan the site by attacking it. You will scan the site upon entering the first node. The disadvantage is that you won't know have any information beforehand.

You can also use the `scan` command once inside the site after having hacked all ICE in a node. This will reveal the part of the site beyond the former ICE node.

Without the scan command, you will automatically scan the new part of the site when you `move` to an unscanned node. This disadvantage is that you can trip tripwires without knowing about them first.