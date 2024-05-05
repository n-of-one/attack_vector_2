package org.n1.av2.backend.entity.user

import org.n1.av2.backend.service.larp.LarpService
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DefaultUserService(
    private val userEntityService: UserEntityService,
    private val larpService: LarpService,
) {
    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        createMandatoryUsers()

        larpService.createMandatoryUsers()
    }


    fun createMandatoryUsers() {
        userEntityService.createDefaultUser("system", UserType.SYSTEM)
        userEntityService.createDefaultUser("admin", UserType.ADMIN)
        userEntityService.createDefaultUser("gm", UserType.GM)
        userEntityService.createDefaultUser("hacker", UserType.HACKER, HackerIcon.CROCODILE)
        userEntityService.createDefaultUser("Stalker", UserType.HACKER, HackerIcon.BEAR)
        userEntityService.createDefaultUser("Paradox", UserType.HACKER, HackerIcon.BULL)
        userEntityService.createDefaultUser("Angler", UserType.HACKER, HackerIcon.SHARK)
    }

}