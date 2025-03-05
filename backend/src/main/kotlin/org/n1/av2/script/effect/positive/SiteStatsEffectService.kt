package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.Layer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.ThemeService
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service

@Service
class SiteStatsEffectService(
    private val nodeEntityService: NodeEntityService,
    private val connectionService: ConnectionService,
    private val themeService: ThemeService,
) : ScriptEffectInterface {

    override val name = "Sitestats"
    override val defaultValue = null
    override val gmDescription = "Show the number of nodes, cores, tripwires and for each ICE type the number and highest strength."

    override fun playerDescription(effect: ScriptEffect) = gmDescription

    override fun validate(effect: ScriptEffect): String? {
        return null
    }

    override fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerState: HackerState): String? {
        return null
    }

    override fun execute(effect: ScriptEffect, tokens: List<String>, hackerState: HackerState): TerminalLockState {
        connectionService.replyTerminalReceive("", "Site stats", "----------")

        val nodes = nodeEntityService.getAll(hackerState.siteId!!)
        connectionService.replyTerminalReceive("Nodes: ${nodes.size}", "")

        reportCountOf(nodes, LayerType.CORE, "", 23)
        reportCountOf(nodes, LayerType.TRIPWIRE, "", 19)
        reportCountOf(nodes, LayerType.TAR_ICE, "(tar)", 14)
        reportCountOf(nodes, LayerType.TANGLE_ICE, "(tangle)", 8)
        reportCountOf(nodes, LayerType.PASSWORD_ICE, "(password)", 6, )
        reportCountOf(nodes, LayerType.WORD_SEARCH_ICE, "(word search)", 5)
        reportCountOf(nodes, LayerType.NETWALK_ICE, "(netwalk)", 3)
        reportCountOf(nodes, LayerType.SWEEPER_ICE, "(minesweeper)", 0)

        connectionService.replyTerminalReceive("")
        return TerminalLockState.UNLOCK
    }

    private fun reportCountOf(nodes: List<Node>, type: LayerType, typeText: String = "", padding: Int = 0) {
        val count = nodes.sumOf { node -> node.layers.count { it.type == type } }
        val padding = " ".repeat(padding)

        if (!type.ice) {
            connectionService.replyTerminalReceive("${themeService.getDefaultName(type)}${padding}: ${count}")
            return
        }

        val maxStrength = nodes.flatMap { node -> node.layers.filter { it.type == type} }
            .maxByOrNull { layer: Layer ->
                val iceLayer = layer as IceLayer
                iceLayer.strength.value
            }
            .let { if (it != null) (it as IceLayer).strength else null}
        val strengthText = if(maxStrength != null) " (strongest: ${maxStrength.description})" else ""

        connectionService.replyTerminalReceive("${themeService.getDefaultName(type)} ICE ${typeText}${padding}: ${count}${strengthText}")
    }
}
