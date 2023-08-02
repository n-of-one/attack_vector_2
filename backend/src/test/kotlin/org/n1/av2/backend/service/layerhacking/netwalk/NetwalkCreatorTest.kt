package org.n1.av2.backend.service.layerhacking.netwalk

import org.junit.jupiter.api.Test
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.service.layerhacking.ice.netwalk.NetwalkCreator

class NetwalkCreatorTest {

    @Test
    fun test() {


        val creator = NetwalkCreator(IceStrength.VERY_WEAK)
        creator.create()

    }
}