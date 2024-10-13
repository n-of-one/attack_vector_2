import {NAVIGATE_PAGE, TASKS} from "../../common/menu/pageReducer";
import {webSocketConnection} from "../../common/server/WebSocketConnection";

export const SERVER_TASKS = "SERVER_TASKS"

export interface Task {
    description: string
    due: number
    userName: string
    siteId: string,
    layerId: string
}

const defaultState: Array<Task> = []


export const tasksReducer = (state: Task[] = defaultState, action: any): Task[] => {
    switch (action.type) {
        case SERVER_TASKS:
            return action.data
        case NAVIGATE_PAGE:
            return navigatePage(action.to)
        default:
            return state
    }
}

let processId: NodeJS.Timeout | null = null

const navigatePage = (page: string) => {
    if (page === TASKS) {
        if (processId === null) {
            processId = setInterval(() => {
                webSocketConnection.send('/admin/monitorTasks', null)
            }, 500)

        }
        return []
    } else {
        if (processId !== null) {
            clearInterval(processId)
            processId = null
        }
    }

    return defaultState
}
