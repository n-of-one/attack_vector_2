package org.n1.mainframe.backend.model.scan

enum class NodeStatus(val level:Int) {
    UNDISCOVERED(0), // scan, run: the existence of this node has not been discovered      [ - no image - ]
    DISCOVERED(1),   // scan, run: existence is known, but the type of node is not known   [discovered]
    TYPE(2),         // scan, run: type and number of services known                       [type]
    CONNECTIONS(3),  // scan, run: the connections of this node are known.                 [type]
    SERVICES(4),     // scan, run: the services of this node are known                     [free, protected, hacked]

}
