import React from 'react';
import {connect} from "react-redux";
import {
    DATA_STORE, MAGIC_EYE, MANUAL_1, MANUAL_2, MANUAL_3, PASSCODE_STORE, PASSWORD_GUESS, RESOURCE_STORE, SYSCON,
    TRANSIT_1,
    TRANSIT_2,
    TRANSIT_3, TRANSIT_4, UNHACKABLE, WORD_SEARCH
} from "../../common/NodeTypesNames";
import NodeImage from "./NodeImage";

/* eslint jsx-a11y/alt-text: 0*/

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({dispatch}) => {
        return (
            <div className="col-lg-2" id="node-library">
                <div className="row">
                    <div className="col-lg-12">
                        <span className="text-muted"> Nodes, drag to editor</span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-lg-12 dark_well node_panel_no_right_padding" id="node-library">
                        <br/>
                        <p className="text-muted">Content nodes</p>
                        <NodeImage type={SYSCON} ice={false} title="Syscon"/>
                        <NodeImage type={DATA_STORE} ice={false} title="Data (info) store"/>
                        <NodeImage type={PASSCODE_STORE} ice={false} title="Passcode store"/>
                        <NodeImage type={RESOURCE_STORE} ice={false} title="Resource store"/>
                        <NodeImage type={TRANSIT_1} ice={false} title="Transit"/>
                        <NodeImage type={TRANSIT_2} ice={false} title="Transit"/>
                        <NodeImage type={TRANSIT_3} ice={false} title="Transit"/>
                        <NodeImage type={TRANSIT_4} ice={false} title="Transit"/>
                        <p className="text-muted">Automated mini game ice nodes</p>
                        <NodeImage type={WORD_SEARCH} ice={true} title="Ice: word search"/>
                        <NodeImage type={MAGIC_EYE} ice={true} title="Ice: magic eye"/>
                        <NodeImage type={PASSWORD_GUESS} ice={true} title="Ice: password guess"/>
                        <NodeImage type={UNHACKABLE} ice={true} title="Ice: unhackable"/>
                        <p className="text-muted">Manual mini game ice nodes</p>
                        <NodeImage type={MANUAL_1} ice={true} title="Ice: manual / gm puzzle"/>
                        <NodeImage type={MANUAL_2} ice={true} title="Ice: manual / gm puzzle"/>
                        <NodeImage type={MANUAL_3} ice={true} title="Ice: manual / gm puzzle"/>
                        <br/>
                        <br/>
                    </div>
                </div>
            </div>
        );
    });
