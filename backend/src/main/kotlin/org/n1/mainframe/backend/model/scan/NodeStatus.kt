package org.n1.mainframe.backend.model.scan

enum class NodeStatus(val level:Int) {
    UNDISCOVERED(0),             // scan, run: the existence of this node has not been discovered
    DISCOVERED(1),               // scan, run: existence is known, but the type of node is not known (the image is blank.)
    TYPE(2),                     // scan, run: type and #services known
    CONNECTIONS(3),              // scan, run: the connections of this node are known.
    SERVICES(4),                 // scan, run: the services of this node are known

}
