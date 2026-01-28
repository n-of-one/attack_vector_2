package org.n1.av2.run.terminal.generic

import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.run.timings.TimingsService
import org.springframework.stereotype.Service

@Service
class DevCommandHelper(
    private val configService: ConfigService,
    private val connectionService: ConnectionService,
    private val timingsService: TimingsService,
) {

    fun checkDevModeEnabled(): Boolean {
        if (!configService.getAsBoolean(ConfigItem.DEV_HACKER_USE_DEV_COMMANDS)) {
            connectionService.replyTerminalReceive("Dev mode not enabled.")
            return false
        }
        return true
    }

    fun setQuickPlaying() {
        if (!checkDevModeEnabled()) return

        timingsService.minimize()
        connectionService.replyTerminalReceive("timings set to quick)")
    }
}
