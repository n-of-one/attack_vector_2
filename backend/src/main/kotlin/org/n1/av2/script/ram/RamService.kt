package org.n1.av2.script.ram

import mu.KotlinLogging
import org.n1.av2.hacker.hacker.Hacker
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hacker.HackerSkillType
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.script.Script
import org.n1.av2.script.ScriptService
import org.n1.av2.script.ScriptState
import org.n1.av2.script.type.ScriptType
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import javax.annotation.PostConstruct

@Configuration
class RamServiceInitializer(
    private val ramService: RamService,
    private val scriptService: ScriptService,
) {
    @PostConstruct
    fun init() {
        ramService.scriptService = scriptService
    }
}

@Service
class RamService(
    private val ramRepository: RamRepository,
    private val hackerEntityService: HackerEntityService,
    private val timeService: TimeService,
    private val userTaskRunner: UserTaskRunner,
    private val currentUserService: CurrentUserService,
) {

    lateinit var scriptService: ScriptService

    private val REFRESH_DURATION = Duration.ofSeconds(10)
    private val HACKING_LOCK_DURATION = Duration.ofMinutes(1)

    private val logger = KotlinLogging.logger {}

    @EventListener(ApplicationStartedEvent::class)
    fun recreateRefreshTimersAfterApplicationRestart() {
        ramRepository.findAll().forEach { ram ->
            if (ram.refreshing > 0) {
                processRefreshDuringServerDown(ram)
            }
            if (ram.lockedUntil != null) {
                processLockedDuringServerDown(ram)
            }
        }
    }

    private fun processRefreshDuringServerDown(ram: RamEntity) {
        if (ram.nextRefresh == null && ram.refreshing == 0) return

        if (ram.nextRefresh == null) {
            // just for safety, if ram.refreshing != 0 then ram.nextRefresh should not be null
            val refreshedRam = sanitize(
                ram.copy(
                    refreshing = 0,
                    free = ram.size,
                )
            )
            ramRepository.save(refreshedRam)
            return
        }

        if (ram.nextRefresh.isBefore(timeService.now()) == true) {
            val durationUntilRefresh = Duration.between(timeService.now(), ram.nextRefresh)
            scheduleRamRefresh(ram.id, durationUntilRefresh)
        } else {
            val nextRefresh = if (ram.refreshing == 1) null else ram.nextRefresh + REFRESH_DURATION

            val refreshedRam = sanitize(
                ram.copy(
                    refreshing = ram.refreshing - 1,
                    free = ram.free + 1,
                    nextRefresh = nextRefresh,
                )
            )
            ramRepository.save(refreshedRam)
            processRefreshDuringServerDown(refreshedRam)
        }
    }

    private fun processLockedDuringServerDown(ram: RamEntity) {
        requireNotNull(ram.lockedUntil)
        if (ram.lockedUntil.isBefore(timeService.now())) {
            val updatedRam = ram.copy(lockedUntil = null)
            ramRepository.save(updatedRam)
        }
        else {
            scheduleRamUnlock(ram)
        }
    }

    fun updateRamFromScripts(userId: String, scriptsAndTypes: List<Pair<Script, ScriptType>>): RamEntity {
        val ram = getRamForUser(userId)

        val loadedSize = scriptsAndTypes.filter { it.first.state == ScriptState.LOADED }.sumOf { it.second.size }
        val refreshingSize = ram.refreshing
        val free = ram.size - loadedSize - refreshingSize

        val updatedRam = sanitize(
            ram.copy(
                loaded = loadedSize,
                free = free
            )
        )
        if (ram != updatedRam) {
            ramRepository.save(updatedRam)
        }
        return updatedRam
    }

    fun getRamById(ramId: RamId): RamEntity {
        return ramRepository.findById(ramId).orElseThrow {
            error("Ram not found with id: $ramId")
        }
    }

    fun getRamForUser(userId: String): RamEntity {
        val ram = ramRepository.findByUserId(userId)
        if (ram != null) {
            return ram
        }

        val hacker: Hacker = hackerEntityService.findForUserId(userId)
        val skillValue = hacker.skillAsIntOrNull(HackerSkillType.SCRIPT_RAM)
        val size = skillValue ?: 0
        val enabled = hacker.skillAsIntOrNull(HackerSkillType.SCRIPT_RAM) != null
        return createRam(userId, size, enabled)
    }

    private fun createRam(userId: String, size: Int, enabled: Boolean): RamEntity {
        fun findExisting(candidate: RamId): Optional<RamEntity> {
            return ramRepository.findById(candidate)
        }

        val id = createId("ram", ::findExisting)

        val ramEntity = RamEntity(
            id = id,
            userId = userId,
            enabled = enabled,
            size = size,
            loaded = 0,
            refreshing = 0,
            free = size,
            nextRefresh = null,
            lockedUntil = null,
        )

        return ramRepository.save(ramEntity)
    }

    fun load(userId: String, size: Int, overrideLocked: Boolean) {
        if (size <= 0) return

        val ram = getRamForUser(userId)
        if (!ram.enabled) error("Cannot load script, hacker cannot use scripts")
        if (ram.free < size) error("Not enough free RAM. Need $size, but only ${ram.free} is free.")
        if (ram.lockedUntil != null && !overrideLocked) error("Cannot load script, hacker has joined a hacking run too recently.")

        val updatedRam = sanitize(
            ram.copy(
                loaded = ram.loaded + size,
                free = ram.free - size
            )
        )
        ramRepository.save(updatedRam)
    }

    fun unload(userId: String, size: Int, overrideLocked: Boolean) {
        if (size <= 0) return

        val ram = getRamForUser(userId)
        val needToRefreshMemory = (ram.lockedUntil != null && !overrideLocked)

        val updatedRam = if (needToRefreshMemory) {
            val nextRefresh = ram.nextRefresh ?: (timeService.now() + REFRESH_DURATION)
            if (ram.nextRefresh == null) {
                scheduleRamRefresh(ram.id, REFRESH_DURATION)
            }
            sanitize(
                ram.copy(
                    loaded = ram.loaded - size,
                    refreshing = ram.refreshing + size,
                    nextRefresh = nextRefresh
                )
            )

        } else {
            sanitize(
                ram.copy(
                    loaded = ram.loaded - size,
                    free = ram.free + size
                )
            )
        }
        ramRepository.save(updatedRam)
    }


    fun useScript(userId: String, size: Int) {
        if (size <= 0) return
        val ram = getRamForUser(userId)

        val startsRefresh = ram.refreshing == 0
        val nextRefresh = if (startsRefresh) timeService.now() + REFRESH_DURATION else ram.nextRefresh

        val updatedRam = sanitize(
            ram.copy(
                loaded = ram.loaded - size,
                refreshing = ram.refreshing + size,
                nextRefresh = nextRefresh,
            )
        )
        ramRepository.save(updatedRam)

        if (startsRefresh) {
            scheduleRamRefresh(ram.id, REFRESH_DURATION)
        }
    }

    private fun scheduleRamRefresh(ramId: RamId, duration: Duration) {
        val identifiers = emptyMap<String, String>()
        userTaskRunner.queue("refresh ram for ${currentUserService.userEntity.name}", identifiers, duration) {
            ramRefreshTick(ramId)
        }
    }

    @ScheduledTask
    fun ramRefreshTick(ramId: RamId) {
        val ram = getRamById(ramId)
        if (ram.refreshing == 0) return

        val newRefreshing = ram.refreshing - 1
        val nextRefresh = if (newRefreshing > 0) timeService.now() + REFRESH_DURATION else null

        val updatedRam = sanitize(
            ram.copy(
                refreshing = newRefreshing,
                free = ram.free + 1,
                nextRefresh = nextRefresh,
            )
        )
        ramRepository.save(updatedRam)
        if (newRefreshing > 0) {
            scheduleRamRefresh(ram.id, REFRESH_DURATION)
        }
        scriptService.sendScriptStatusToCurrentUser()
    }

    fun alterRamSize(userId: String, newSize: Int) {
        val ram = getRamForUser(userId)
        if (newSize == 0) {
            val newRam = sanitize(
                ram.copy(
                    size = 0,
                    free = 0,
                    loaded = 0,
                    refreshing = 0,
                    nextRefresh = null,
                    lockedUntil = null,
                    enabled = false,
                )
            )
            ramRepository.save(newRam)
            return
        }

        val newFree = newSize - ram.loaded - ram.refreshing

        if (newFree >= 0) {
            val newRam = sanitize(
                ram.copy(
                    size = newSize,
                    free = newFree,
                    enabled = true,
                )
            )
            ramRepository.save(newRam)
        } else {
            val newRam = sanitize(
                ram.copy(
                    size = newSize,
                    free = newSize,
                    loaded = 0,
                    refreshing = 0,
                    nextRefresh = null,
                    enabled = true,
                )
            )
            ramRepository.save(newRam)
            scriptService.unloadAllScriptsForShrinkedRam(userId)
        }
        scriptService.sendScriptStatusForUser(userId)
    }

    private fun sanitize(ram: RamEntity): RamEntity {
        val sanitizedLoaded = (ram.loaded.coerceAtMost(ram.size)).coerceAtLeast(0)
        val sanitizeRefreshing = (ram.refreshing.coerceAtMost(ram.size)).coerceAtLeast(0)
        val sanitizedFree = (ram.size - sanitizedLoaded - sanitizeRefreshing).coerceAtLeast(0)

        val sanitized = ram.copy(
            loaded = sanitizedLoaded,
            refreshing = sanitizeRefreshing,
            free = sanitizedFree
        )

        if (sanitized == ram ) return ram

        if (sanitizedLoaded != ram.loaded) logger.error("Sanitized loaded RAM for user ${ram.userId} from ${ram.loaded} -> $sanitizedLoaded")
        if (sanitizeRefreshing != ram.refreshing) logger.error("Sanitized refreshing RAM for user ${ram.userId} from ${ram.loaded} -> $sanitizeRefreshing")
        if (sanitizedFree != ram.free) logger.error("Sanitized free RAM for user ${ram.userId} from ${ram.loaded} -> $sanitizedFree")

        return sanitized

    }

    fun startHack(userId: String) {
        val ram = getRamForUser(userId)
        val updatedRam = ram.copy(lockedUntil = timeService.now() + HACKING_LOCK_DURATION)
        ramRepository.save(updatedRam)
        scriptService.sendScriptStatusToCurrentUser()

        scheduleRamUnlock(updatedRam)
    }

    private fun scheduleRamUnlock(ram: RamEntity) {
        val durationUntilUnlock = Duration.between(timeService.now(), ram.lockedUntil)
        val identifiers = emptyMap<String, String>()
        userTaskRunner.queue("unlock ram for ${ram.userId}", identifiers, durationUntilUnlock) {
            unlockRam(ram.userId)
        }
    }

    @ScheduledTask
    private fun unlockRam(userId: String) {
        val ram = getRamForUser(userId)
        val updatedRam = ram.copy(lockedUntil = null)
        ramRepository.save(updatedRam)
        scriptService.sendScriptStatusToCurrentUser()
    }
}
