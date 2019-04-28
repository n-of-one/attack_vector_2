const orderByDistance = (data) => {
    const {scan, site} = data;
    const {nodes, connections} = site;
    const nodesById = {};
    nodes.forEach((node) => {
        nodesById[node.id] = node;
        node.distance = scan.nodeScanById[node.id].distance;
    });

    nodes.sort((a, b) => a.distance - b.distance);
    connections.sort((a, b) => nodesById[a.fromId].distance - nodesById[b.fromId].distance);
};


export {orderByDistance}