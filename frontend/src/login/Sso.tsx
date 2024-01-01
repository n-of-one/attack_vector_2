import React from "react";
import {larp} from "../common/Larp";

export const Banner = () => {
    return (<>
            <br/>
            <br/>
            <br/>
            <div className="row">
                <div className="d-flex justify-content-center">
                    <span className="text"><pre>
                        &nbsp; &nbsp;&nbsp;      _|_|      _|      _|                          _|            _|      _|                        _|                                _|_|<br/>
                        &nbsp; &nbsp; &nbsp; &nbsp; _|    _|  _|_|_|_|_|_|_|_|    _|_|_|    _|_|_|  _|  _|        _|      _|    _|_|      _|_|_|  _|_|_|_|    _|_|    _|  _|_|      _|    _|<br/>
                        &nbsp; &nbsp; &nbsp; &nbsp; _|_|_|_|    _|      _|      _|    _|  _|        _|_|          _|      _|  _|_|_|_|  _|          _|      _|    _|  _|_|              _|<br/>
                        &nbsp; &nbsp; &nbsp; &nbsp; _|    _|    _|      _|      _|    _|  _|        _|  _|          _|  _|    _|        _|          _|      _|    _|  _|              _|<br/>
                        &nbsp; &nbsp; &nbsp; &nbsp; _|    _|      _|_|    _|_|    _|_|_|    _|_|_|  _|    _|          _|        _|_|_|    _|_|_|      _|_|    _|_|    _|            _|_|_|_|<br/>
                        _|_|_|<br/>
                        </pre>
                    </span>
                    <br/>
                    <br/>
                    <br/>
                </div>
            </div>
        </>
    )
}


export const BannerPage = () => {
    return (
        <div className="container" data-bs-theme="dark">
            <Banner/>
            <div className="row">
                <div className="d-flex justify-content-center">
                    <button className="btn btn-info" style={{opacity: 0.7}} onClick={() => {
                        window.location.href = larp.loginUrl
                    }}>Login
                    </button>
                </div>
            </div>
        </div>
    )
}
