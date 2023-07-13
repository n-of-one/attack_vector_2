package org.n1.av2.backend.entity.site.layer.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.service.layerhacking.ice.tar.TarCreator
import org.n1.av2.backend.service.layerhacking.ice.tar.TarCreator.Companion.defaultTimeHackerGroup

class TarIceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean,
    var totalUnits: Int,
    var time1Level1Hacker: String,
    var time1Level5Hacker: String,
    var time5Level10Hackers: String

) : IceLayer(id, type, level, name, note, strength, hacked) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.TAR_ICE, level, defaultName, "",
                IceStrength.AVERAGE, false,
                TarCreator.totalUnitsByStrength[IceStrength.AVERAGE]!!,
                defaultTimeHackerGroup(IceStrength.AVERAGE, 1, 1),
                defaultTimeHackerGroup(IceStrength.AVERAGE, 5, 1),
                defaultTimeHackerGroup(IceStrength.AVERAGE, 10, 5),
                )

    override fun updateInternal(key: String, value: String): Boolean {
        if (key == STRENGTH) {
            super.updateInternal(key, value)
            this.totalUnits = TarCreator.totalUnitsByStrength[strength]!!
            this.time1Level1Hacker = defaultTimeHackerGroup(strength, 1, 1)
            this.time1Level5Hacker = defaultTimeHackerGroup(strength, 5, 1)
            this.time5Level10Hackers = defaultTimeHackerGroup(strength, 10, 5)
            return true
        }
        return super.updateInternal(key, value)
    }

}