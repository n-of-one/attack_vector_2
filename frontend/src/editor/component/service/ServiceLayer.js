import React from 'react';
import {connect} from "react-redux";

const mapDispatchToProps = (dispatch) => {
    return {
    }
};
let mapStateToProps = (state) => {
    return {
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({layer, layourCount}) => {

        let downClickable = (layer > 1);
        let upClickable = (layer !== 0 && layer < (layourCount-1));

        let downHtml = downClickable ? (<a className="textLink">◀</a>) : (<span>◀</span>);
        let upHtml = upClickable ? (<a className="textLink">▶</a>) : (<span>▶</span>);


        return (
            <div className="row form-group serviceFieldRow">
                <div className="col-lg-3 serviceLabel">Layer</div>
                <div className="col-lg-8">
                    <div className="text-muted strong service_text_label">
                        0&nbsp;{downHtml}&nbsp;{upHtml}
                     </div>
                </div>
            </div>
        );
    });
