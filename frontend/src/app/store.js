import {createStore, combineReducers} from 'redux'
import pageReducer from "../reducer/pageReducer";


const reducer = combineReducers({
    page: pageReducer,
});

const initStore = () => {
    return createStore(reducer);
};

export default initStore;