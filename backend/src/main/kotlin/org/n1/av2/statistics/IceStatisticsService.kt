package org.n1.av2.statistics

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.format.DateTimeFormatter

@Service
class IceStatisticsService(
    private val iceHackStatisticRepo: IceHackStatisticRepo,
    private val iceService: IceService,
    private val nodeEntityService: NodeEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val timeService: TimeService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService,
) {

    fun hackerEnterIce(iceId: String, user: UserEntity) {
        val statistic = getOrCreateStatistic(iceId)
        if (statistic.state != IceHackState.IN_PROGRESS) return // Ice is already hacked, no need to update anything.

        val statisticWithUser = addUser(statistic, user)
        iceHackStatisticRepo.save(statisticWithUser)
    }

    private fun addUser(statistic: IceHackStatistic, user: UserEntity): IceHackStatistic {
        if (statistic.participants.contains(user.name)) {
            return statistic
        }

        return statistic.copy(
            participants = statistic.participants + user.name,
            hackerConnectDurationSeconds = statistic.hackerConnectDurationSeconds + (user.name to 0)
        )
    }

    private fun getOrCreateStatistic(iceId: String): IceHackStatistic {
        iceHackStatisticRepo.findByIceId(iceId)?.let { return it }

        val layerId = iceService.findLayerIdForIceId(iceId)
        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId)
        if (layer !is IceLayer) error("Hacking a layer that is not an ICE layer. layerId=${layerId}")

        val siteProperties = sitePropertiesEntityService.getBySiteId(node.siteId)

        val id = createId("stat", iceHackStatisticRepo::findById)

        return IceHackStatistic(
            id = id,
            iceId = iceId,
            layerId = layerId,
            iceType = layer.type,
            strength = layer.strength,
            participants = emptyList(),
            hackTimeFromStartSeconds = 0,
            hackerConnectDurationSeconds = emptyMap(),
            totalHackerSeconds = 0,
            sweeperResets = null,
            sweeperLockouts = null,
            siteName = siteProperties.name,
            nodeNetworkId = node.networkId,
            layerLevel = layer.level,
            hackStartTimestamp = timeService.now(),
        )
    }

    fun hackerLeaveIce(hackerState: HackerState, user: UserEntity) {
        val statistic = getById(hackerState.iceId!!)
        if (statistic.state != IceHackState.IN_PROGRESS) return // Ice is already hacked, no need to update anything.

        val updatedStatistic = updateStatisticForHackerLeave(statistic, hackerState, user)

        iceHackStatisticRepo.save(updatedStatistic)
    }

    private fun updateStatisticForHackerLeave(statistic: IceHackStatistic, hackerState: HackerState, user: UserEntity): IceHackStatistic {
        val hackerConnectDurationSeconds: Int = statistic.hackerConnectDurationSeconds[user.name] ?: error("Hacker not known")
        val thisDurationSecond = Duration.between(hackerState.iceConnectTimestamp!!, timeService.now()).seconds.toInt()

        val newDuration: Int = hackerConnectDurationSeconds + thisDurationSecond

        return statistic.copy(
            hackerConnectDurationSeconds = statistic.hackerConnectDurationSeconds + (user.name to newDuration)
        )

    }

    private fun getById(iceId: String): IceHackStatistic {
        return iceHackStatisticRepo.findByIceId(iceId) ?: error("IceHack Statistic for $iceId not found.")
    }

    fun finishHackingIce(iceId: String, solution: IceHackState) {
        val initialStatistic = getOrCreateStatistic(iceId)

        val statistic = updateTimeSpentByEachHacker(initialStatistic)

        val finalStatistics = statistic.copy(
            hackTimeFromStartSeconds = Duration.between(statistic.hackStartTimestamp, timeService.now()).seconds.toInt(),
            totalHackerSeconds = statistic.hackerConnectDurationSeconds.values.sum(),
            state = solution,
        )
        iceHackStatisticRepo.save(finalStatistics)
    }

    private fun updateTimeSpentByEachHacker(statistic: IceHackStatistic): IceHackStatistic {
        val hackerStates = hackerStateEntityService.findByIceId(statistic.iceId)

        val updatedState = hackerStates
            .filter { hackerState -> hackerState.iceId == statistic.iceId }
            .fold(statistic) { updatingStatistic: IceHackStatistic, hackerState: HackerState ->
                val user = userEntityService.getById(hackerState.userId)
                updateStatisticForHackerLeave(updatingStatistic, hackerState, user)
            }
        return updatedState
    }

    fun siteReset(siteId: String) {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val statisticsForSite = iceHackStatisticRepo.findBySiteName(siteProperties.name)
        statisticsForSite.forEach { statistic ->
            if (statistic.state == IceHackState.IN_PROGRESS) {
                val updatedStatistic = updateTimeSpentByEachHacker(statistic)
                iceHackStatisticRepo.save(updatedStatistic)

                finishHackingIce(statistic.iceId, IceHackState.HACK_FAILED_SITE_RESET)
            }
        }
    }

    fun getAllAsCsv(): String {
        return csvHeader() +
            iceHackStatisticRepo.findAll().joinToString("\n") { toCsvLine(it) }

    }

    private fun csvHeader() = "ice type;strength;" +
        "state;hack time seconds;hack time total seconds;" +
        "hacker count;hackers;seconds per hacker;" +
        "site:node:level;" +
        "start;" +
        "sweeper resets;sweeper mines exploded;iceId" +
        "\n"

    private fun toCsvLine(statistic: IceHackStatistic): String {

        val iceType = friendlyIceName(statistic.iceType)
        val participants = statistic.participants.joinToString("|")
        val hackerConnectDurationSeconds = statistic.hackerConnectDurationSeconds
            .map { entry: Map.Entry<String, Int> -> "${entry.key}:${entry.value}" }
            .joinToString("|")

        val startTimestamp = statistic.hackStartTimestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val sweeperResets = statistic.sweeperResets?.toString() ?: ""
        val sweeperLockouts = statistic.sweeperLockouts?.toString() ?: ""

        return "${iceType};${statistic.strength.toString().lowercase()};" +
            "${statistic.state.toString().lowercase()};${statistic.hackTimeFromStartSeconds};${statistic.totalHackerSeconds};" +
            "${statistic.participants.size};${participants};${hackerConnectDurationSeconds};" +
            "${statistic.siteName}:${statistic.nodeNetworkId}:${statistic.layerLevel};" +
            "${startTimestamp};" +
            "${sweeperResets};${sweeperLockouts};${statistic.iceId}"
    }

    private fun friendlyIceName(layerType: LayerType) =
        when (layerType) {
            LayerType.NETWALK_ICE -> "netwalk"
            LayerType.TANGLE_ICE -> "tangle"
            LayerType.WORD_SEARCH_ICE -> "word search"
            LayerType.SWEEPER_ICE -> "sweeper"
            LayerType.TAR_ICE -> "tar"
            LayerType.PASSWORD_ICE -> "password"
            else -> error("Unknown ICE type")
        }

    fun sweeperLockout(iceId: String) {
        val statistic = getOrCreateStatistic(iceId)
        val updatedStatistic = statistic.copy(
            sweeperLockouts = (statistic.sweeperLockouts ?: 0) + 1
        )
        iceHackStatisticRepo.save(updatedStatistic)
    }

    fun sweeperReset(iceId: String) {
        val statistic = getOrCreateStatistic(iceId)
        val updatedStatistic = statistic.copy(
            sweeperResets = (statistic.sweeperResets ?: 0) + 1
        )
        iceHackStatisticRepo.save(updatedStatistic)
    }
}
