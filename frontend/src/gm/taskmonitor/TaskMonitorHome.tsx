import React from "react";
import {Task} from "./TaskReducer";
import {useSelector} from "react-redux";
import {AdminRootState} from "../../admin/AdminRootReducer";


export const TaskMonitorHome = () => {


    const taskInformation = useSelector((state: AdminRootState) => state.tasks);
    return (
        <div className="row">
            <div className="col-lg-2">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <strong>Active tasks</strong><br/>
                </div>
            </div>
            <div className="col-lg-5 rightPane rightPane">
                <div className="siteMap">
                    <table className="table table-sm text-muted text" id="sitesTable">
                        <thead>
                        <tr>
                            <td className="strong">Due</td>
                            <td className="strong">Action</td>
                            <td className="strong">User</td>
                            <td className="strong">Site</td>
                            <td className="strong">NodeId</td>
                        </tr>
                        </thead>
                        <tbody>
                        {renderTasks(taskInformation)}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    )

}

const renderTasks = (tasks: Array<Task>) => {
    if (tasks.length === 0) {
        return <tr>
            <td>No tasks</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
    }

    const now = new Date().getTime()


    return <>
        {
            tasks.map((task: Task) => {
                const id = `${task.due}${task.userName}${task.siteId}${task.layerId}`;
                return (
                    <tr key={id}>
                        <td>{`${(task.due-now)}`}</td>
                        <td>{task.description}</td>
                        <td>{task.userName}</td>
                        <td>{task.siteId}</td>
                        <td>{task.layerId}</td>
                    </tr>)
            })
        }
    </>

}
