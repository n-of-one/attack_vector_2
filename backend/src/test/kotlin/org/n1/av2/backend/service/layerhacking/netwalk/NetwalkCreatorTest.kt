package org.n1.av2.backend.service.layerhacking.netwalk

import org.junit.jupiter.api.Test
import org.n1.av2.backend.entity.site.enums.IceStrength

class NetwalkCreatorTest {

    @Test
    fun test() {


        val creator = NetwalkCreator(IceStrength.VERY_WEAK)
        val grid = creator.create()

    }
}