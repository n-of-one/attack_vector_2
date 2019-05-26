import React from 'react';
import {connect} from "react-redux";
import SilentLink from "../../../common/component/SilentLink";
import Glyphicon from "../../../common/component/Glyphicon";

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({name}) => {

        return (
            <div className="row form-group serviceFieldTopRow">
                <div className="col-lg-3 control-label serviceLabel">Service</div>
                <div className="col-lg-8">
                    <div className="text-muted strong service_text_label text_gold">{name}
                        <span className="pull-right">
                            <SilentLink onClick={() => { alert('jo'); }}>
                                <Glyphicon name="glyphicon-remove" size="18px"/>
                            </SilentLink>
                        </span>
                    </div>
                </div>
            </div>

        );
    });
