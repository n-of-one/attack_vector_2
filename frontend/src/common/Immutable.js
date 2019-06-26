import {assertNotNullUndef} from "./Assert";

const updateArrayById = (newElementData, array, id, idField = "id") => {
    const nodeIndex = findElementIndex(array, id, idField);
    const oldElement = array[nodeIndex];
    const newElement = {...oldElement, ...newElementData};
    const newArray = [...array];
    newArray.splice(nodeIndex, 1, newElement);
    return newArray;
};

const findElementIndex = (array, id, idField = "id") => {
    let elementIndex = null;
    array.forEach((element, index) => {
        if (element[idField] === id) {
            elementIndex = index;
        }
    });
    assertNotNullUndef(elementIndex, {array, id});
    return elementIndex;
};

const findElementById = (array, id, idField = "id") => {
    const index = findElementIndex(array, id, idField);
    return array[index];
};

export {updateArrayById, findElementIndex, findElementById};