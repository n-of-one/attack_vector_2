import React from "react";

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
            window.location.href = "/adminLogin"
        }
    }

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
                    </pre>
                    <br/>
                </div>
            </div>
        </>
    )
}




