package org.n1.mainframe.backend.config

import org.apache.catalina.connector.CoyoteInputStream
import org.n1.mainframe.backend.util.createId
import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.io.InputStream
import java.io.InputStreamReader
import java.security.Principal
import kotlin.text.Charsets.UTF_8

class AssignPrincipalHandshakeHandler : DefaultHandshakeHandler() {

    fun rnull(ignore: String): Any? {
        return null
    }

    override fun determineUser(request: ServerHttpRequest, wsHandler: WebSocketHandler?,
                               attributes: Map<String, Any>?): Principal {

        val bodyInputStream: InputStream = request.body as InputStream
        val bodyReader = InputStreamReader(bodyInputStream, UTF_8)
        val lines = bodyReader.readLines()
        val cookie = request.headers["cookie"]!!.first()
        val a = 1 + 2


        val name = createId("client", ::rnull )
        return Principal { name }
    }

}