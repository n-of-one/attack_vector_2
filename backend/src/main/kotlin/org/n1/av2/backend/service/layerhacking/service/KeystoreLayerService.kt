package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.entity.site.layer.other.KeyStoreLayer
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class KeystoreLayerService(
    private val stompService: StompService,
    private val keystoreService: KeystoreService,

    ){
    fun hack(layer: KeyStoreLayer) {
        if (layer.iceLayerId == null) {
            stompService.replyTerminalReceive("[error]service misconfiguration[/] this keystore is inactive due to misconfiguration.")
            return
        }
        val password = keystoreService.getIcePassword(layer.iceLayerId!!).password

        stompService.replyTerminalReceive("hacked. Password found: [primary]${password}")
    }

    fun connect(layer: KeyStoreLayer) {
        stompService.replyTerminalReceive("Access to UI denied")
    }
}