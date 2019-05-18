const TRANSIT_1 = { name: "transit_1", dir: "reg"};
const TRANSIT_2 = { name: "transit_2", dir: "reg"};
const TRANSIT_3 = { name: "transit_3", dir: "reg"};
const TRANSIT_4 = { name: "transit_4", dir: "reg"};
const SYSCON = { name: "syscon", dir: "reg"};
const DATA_STORE = { name: "data_store", dir: "reg"};
const PASSCODE_STORE = { name: "passcode_store", dir: "reg"};
const RESOURCE_STORE = { name: "resource_store", dir: "reg"};
const ICE_1= { name: "ice_1", dir: "ice"};
const ICE_2 = { name: "ice_2", dir: "ice"};
const ICE_3 = { name: "ice_3", dir: "ice"};
const UNHACKABLE = { name: "unhackable", dir: "ice"};
const MANUAL_1 = { name: "manual_1", dir: "ice"};
const MANUAL_2 = { name: "manual_2", dir: "ice"};
const MANUAL_3 = { name: "manual_3", dir: "ice"};

const all = {
    TRANSIT_1, TRANSIT_2, TRANSIT_3, TRANSIT_4, SYSCON, DATA_STORE,
    PASSCODE_STORE, RESOURCE_STORE, ICE_1, ICE_2, ICE_3,
    UNHACKABLE, MANUAL_1, MANUAL_2, MANUAL_3};

const toType = (name) => {
    const match = Object.entries(all).find(
        ([key, _]) => { return key === name }
    );
    if (match) {
       return match[1];
    }
    return null;
};

export { toType,
    TRANSIT_1, TRANSIT_2, TRANSIT_3, TRANSIT_4, SYSCON, DATA_STORE,
    PASSCODE_STORE, RESOURCE_STORE, ICE_1, ICE_2, ICE_3,
    UNHACKABLE, MANUAL_1, MANUAL_2, MANUAL_3};