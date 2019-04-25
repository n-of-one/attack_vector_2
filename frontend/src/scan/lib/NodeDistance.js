const orderByDistance = (site) => {
    const {nodes, connections, startNodeId} = site;
    const nodesById = {};
    nodes.forEach((node) => {
        nodesById[node.id] = node;
        node.connections = [];
    });

    connections.forEach((connection) => {
        const from = nodesById[connection.fromId];
        const to = nodesById[connection.toId];
        from.connections.push(to);
        to.connections.push(from);
    });

    const startNode = nodesById[startNodeId];
    nodes.forEach((node) => {
        node.distance = null
    });
    walkConnections(startNode, 0);

    nodes.sort((a, b) => a.distance - b.distance);
    connections.sort((a, b) => nodesById[a.fromId].distance - nodesById[b.fromId].distance);
};

const walkConnections = (node, currentDistance) => {
    node.distance = currentDistance;
    node.connections.forEach((node) => {
        if (node.distance === null) {
            walkConnections(node, currentDistance + 1);
        }
    });
};

export {orderByDistance}