const orderByDistance = (site) => {
    const {nodes, connections} = site;
    const nodesById = {};
    nodes.forEach((node) => {
        nodesById[node.id] = node;
    });

    nodes.sort((a, b) => a.distance - b.distance);
    connections.sort((a, b) => nodesById[a.fromId].distance - nodesById[b.fromId].distance);
};


export {orderByDistance}