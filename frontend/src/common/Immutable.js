import {assertNotNullUndef} from "./Assert";

const updateArray = (newElementData, array, id) => {
    const nodeIndex = findElementIndex(array, id);
    const oldElement = array[nodeIndex];
    const newElement = {...oldElement, ...newElementData};
    const newArray = [...array];
    newArray.splice(nodeIndex, 1, newElement);
    return newArray;
};

const findElementIndex = (array, id) => {
    let elementIndex = null;
    array.forEach((element, index) => {
        if (element.id === id) {
            elementIndex = index;
        }
    });
    assertNotNullUndef(elementIndex, {array, id});
    return elementIndex;
};

const findElementById = (array, id) => {
    const index = findElementIndex(array, id);
    return array[index];
};

export {updateArray, findElementIndex, findElementById};