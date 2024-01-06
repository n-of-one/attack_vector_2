import React from "react";
import {larp} from "../common/Larp";
import {SilentLink} from "../common/component/SilentLink";

export const Banner = () => {
    return (<>
            <div className="row">
                <div className="d-flex justify-content-center">
                    <span className="text dark"><pre>
                        <br/>
                        <br/>
                        <br/>
                        <br/>
                        <br/>
                        <br/>
                        <br/>
                        <br/>

                        &nbsp; &nbsp;&nbsp;      _|_|      _|      _|                          _|            _|      _|                        _|                              <i onClick={adminLogin}>  _|_|</i><br/>
                        &nbsp; &nbsp; &nbsp; &nbsp; _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_| <i onClick={adminLogin}>     _|    _|</i><br/>
                        &nbsp; &nbsp; &nbsp; &nbsp; _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|     <i onClick={adminLogin}>         _|</i><br/>
                        &nbsp; &nbsp; &nbsp; &nbsp; _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|       <i onClick={adminLogin}>       _|</i><br/>
                        &nbsp; &nbsp; &nbsp; &nbsp; _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _|       <i onClick={adminLogin}>     _|_|_|_|</i><br/>
                    <br/>
                    <br/>
                    <br/>
                    <br/>
                        </pre>
                    </span>
                </div>
            </div>
        </>
    )
}

const adminLogin = () => {
    window.location.href = "/adminLogin"
}


