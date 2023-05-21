import React from "react";
import {larp} from "../common/Larp";

// const url = "https://ic.eosfrontier.space/assets/idandgroups.php"
// // const url = "https://ic.eosfrontier.space"
// // const url = "http://av.eosfrontier.space/about"
//
// const fetchData = async (set: (input: string) => void) => {
//     const response = await fetch(url, {credentials: 'include'})
//     // const response = await fetch(url)
//     const text = await response.text()
//     set(`text: ${text}`)
// }
//
//
// export const Sso = () => {
//
//     const [response, setResponse] = React.useState<string>("not received yet.")
//
//     useEffect(() => {
//         fetchData(setResponse)
//     }, [])
//
//     return <div className="text"> Response: {response}</div>
// }

export const BannerPage = () => {
    return (
        <div className="container" data-bs-theme="dark">
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