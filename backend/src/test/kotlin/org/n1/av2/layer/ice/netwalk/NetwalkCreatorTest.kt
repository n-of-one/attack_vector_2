package org.n1.av2.layer.ice.netwalk

import org.junit.jupiter.api.Test
import org.n1.av2.site.entity.enums.IceStrength

class NetwalkCreatorTest {

    @Test
    fun test() {


        val creator = NetwalkCreator(IceStrength.VERY_WEAK)
        creator.create()

    }
}
