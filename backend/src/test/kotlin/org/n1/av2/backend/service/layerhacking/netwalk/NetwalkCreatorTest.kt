package org.n1.av2.backend.service.layerhacking.netwalk

import org.junit.jupiter.api.Test
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.layer.ice.netwalk.NetwalkCreator

class NetwalkCreatorTest {

    @Test
    fun test() {


        val creator = NetwalkCreator(IceStrength.VERY_WEAK)
        creator.create()

    }
}
