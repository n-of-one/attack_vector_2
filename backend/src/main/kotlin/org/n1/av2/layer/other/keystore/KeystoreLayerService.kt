package org.n1.av2.layer.other.keystore

import org.n1.av2.platform.connection.ConnectionService
import org.springframework.stereotype.Service

@Service
class KeystoreLayerService(
    private val connectionService: ConnectionService,
    private val keystoreService: KeystoreService,

    ){
    fun hack(layer: KeyStoreLayer) {
        if (layer.iceLayerId == null) {
            connectionService.replyTerminalReceive("[error]service misconfiguration[/] this keystore is inactive due to misconfiguration.")
            return
        }
        val password = keystoreService.getIcePassword(layer.iceLayerId!!).password

        connectionService.replyTerminalReceive("hacked. Password found: [primary]${password}")
    }

}
