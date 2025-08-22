import React from "react";
import {SilentLink} from "../common/component/SilentLink";

interface Props {
    hiddenAdminLogin?: boolean
    image?: boolean
}


export const Banner = ({hiddenAdminLogin = false, image = false}: Props) => {

    if (image) {
        return (<>
            <br/>
            <div className="row">
                <div className="d-flex justify-content-center">
                    <img src="/img/banner.png" className="img-fluid" alt="Banner"/>
                </div>
            </div>
            <br/>
        </>)
    }

    const adminLogin = () => {
        if (hiddenAdminLogin) {

            const next = new URLSearchParams(window.location.search).get("next")
            if (next) {
                window.location.href = "/adminLogin?next=" + next
            } else {
                window.location.href = "/adminLogin"
            }
        }
    }

    const version = <>2.11.1</>

    return (<>
            <div className="row">
                <div className="d-flex justify-content-center">
                    <pre className="text dark">
                        <br/>
                        <br/>
                        <br/>
                        &nbsp;  #|#|      #|      #|                          #|            #|      #|                        #|                              <i onClick={adminLogin}>  #|#|</i><br/>
                        &nbsp;#|    #|  #|#|#|#|#|#|#|#|    #|#|#|    #|#|#|  #|  #|        #|      #|    #|#|      #|#|#|  #|#|#|#|    #|#|    #|  #|#|      <i onClick={adminLogin}>#|    #|</i><br/>
                        &nbsp;#|#|#|#|    #|      #|      #|    #|  #|        #|#|          #|      #|  #|#|#|#|  #|          #|      #|    #|  #|#|          <i onClick={adminLogin}>    #|</i><br/>
                        &nbsp;#|    #|    #|      #|      #|    #|  #|        #|  #|          #|  #|    #|        #|          #|      #|    #|  #|            <i onClick={adminLogin}>  #|</i><br/>
                        &nbsp;#|    #|      #|#|    #|#|    #|#|#|    #|#|#|  #|    #|          #|        #|#|#|    #|#|#|      #|#|    #|#|    #|            <i onClick={adminLogin}>#|#|#|#|</i><br/>
                        <br/>
                        <span className="text dark ">&nbsp;Version: <SilentLink href="https://github.com/n-of-one/attack_vector_2/blob/main/RELEASE_NOTES.md">{version}</SilentLink></span>
                    </pre>
                    <br/>
                </div>
            </div>
        </>
    )
}




