package org.n1.av2.platform.connection

import org.n1.av2.hacker.hackerstate.HackerConnectionService
import org.n1.av2.platform.connection.ConnectionType.WS_HACKER_MAIN
import org.n1.av2.platform.connection.ConnectionType.WS_NETWORK_APP
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct


// Prevent circular connection
@Configuration
class ConnectDisconnectServiceInit(
    val userTaskRunner: UserTaskRunner,
    val hackerConnectionService: HackerConnectionService,
    val connectDisconnectService: ConnectDisconnectService,
) {

    @PostConstruct
    fun postConstruct() {
        connectDisconnectService.userTaskRunner = userTaskRunner
        connectDisconnectService.hackerConnectionService = hackerConnectionService
    }
}

@Service
class ConnectDisconnectService {

    lateinit var userTaskRunner: UserTaskRunner
    lateinit var hackerConnectionService: HackerConnectionService


    fun connect(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {

            if (userPrincipal.type == WS_HACKER_MAIN || userPrincipal.type == WS_NETWORK_APP) {
                hackerConnectionService.browserConnect(userPrincipal)
            }
        }
    }

    fun browserDisconnect(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {

            if (userPrincipal.type == WS_HACKER_MAIN || userPrincipal.type == WS_NETWORK_APP) {
                hackerConnectionService.browserDisconnect(userPrincipal)
            }
        }
    }

    fun sendTime(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            hackerConnectionService.sendTime()
        }
    }

}
