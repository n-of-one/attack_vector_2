import React from 'react';

export default ({value, save}) => {

    const saveInternal = (event) => {
        save(event.target.value);
    };

        return (
            <div className="row form-group layerFieldRow">
                <div className="col-lg-3 layerLabel">Strength</div>
                <div className="col-lg-5 noRightPadding">
                    <select className="form-control" onChange={saveInternal} value={value}>
                        <option value="VERY_WEAK">Very weak</option>
                        <option value="WEAK">Weak</option>
                        <option value="AVERAGE">Average</option>
                        <option value="STRONG">Strong</option>
                        <option value="VERY_STRONG">Very Strong</option>
                        <option value="IMPENETRABLE">Impenetrable</option>
                        <option value="UNKNOWN">Unknown</option>
                    </select>
                </div>
                <div className="col-lg-1 layerHelpColumn">
                    <span className="badge helpBadge" title="Stronger Ice takes longer to hack or require scripts to overcome." >?</span>
                </div>
            </div>
        );
    };
