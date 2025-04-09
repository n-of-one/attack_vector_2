package org.n1.av2.statistics

import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Document
data class IceHackStatistic(
    @Id val id: String,
    @Indexed val iceId: String,
    val layerId: String,
    val iceType: LayerType,
    val strength: IceStrength,
    val tangleClusters: Int?, // specific to Tangle ICE: how many clusters where there?
    val state: IceHackState = IceHackState.IN_PROGRESS,
    val participants: List<String>, // hacker names (not user-ids)
    val hackStartTimestamp: ZonedDateTime,
    val hackTimeFromStartSeconds: Int, // number of seconds to hack since first hacker starts hacking it.
    val hackerConnectDurationSeconds: Map<String, Int>, // for each hacker (hacker-name) the number of seconds it is connected. Needed to calculate: totalHackerSeconds
    val totalHackerSeconds: Int, // total number of seconds of how long all combined hackers were connected to this ICE while it was being hacked.
    val sweeperResets: Int?,  // specific to Sweeper ICE: how many times was this ICE restarted?
    val sweeperLockouts: Int?,  // specific to Sweeper ICE: how many times was a hacker locked out by clicking on a mine

    // The properties below are to make it easy to export this statistic, and have a GM reference the ICE in the site.
    val siteName: String,
    val nodeNetworkId: String,
    val layerLevel: Int,
)

enum class IceHackState {
    IN_PROGRESS,
    HACKED,
    USED_PASSCODE,
    USED_SCRIPT,
    USED_DEV_MODE,
    HACK_FAILED_SITE_RESET,
}


@Repository
interface IceHackStatisticRepo : MongoRepository<IceHackStatistic, String> {
    fun findByIceId(iceId: String): IceHackStatistic?
    fun findBySiteName(siteName: String): List<IceHackStatistic>
}
