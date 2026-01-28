package org.n1.av2.layer.ice.tar

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceLayerFields.STRENGTH
import org.n1.av2.layer.ice.tar.TarCreator.Companion.defaultTimeHackerGroup
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType

class TarIceLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean,
    var totalUnits: Int,
    var time1Level1Hacker: String,
    var time1Level5Hacker: String,
    var time5Level10Hackers: String,
    original: IceLayer?,

    ) : IceLayer(id, LayerType.TAR_ICE, level, name, note, strength, hacked, original) {

    constructor(id: String, level: Int, name: String, note: String, strength: IceStrength, hacked: Boolean, original: IceLayer? = null) :
        this(
            id, LayerType.TAR_ICE, level, name, note, strength, hacked,
            TarCreator.totalUnitsByStrength[strength]!!,
            defaultTimeHackerGroup(IceStrength.AVERAGE, 1, 1),
            defaultTimeHackerGroup(IceStrength.AVERAGE, 5, 1),
            defaultTimeHackerGroup(IceStrength.AVERAGE, 10, 5),
            original
        )

    constructor(id: String, level: Int, defaultName: String) :
        this(
            id, LayerType.TAR_ICE, level, defaultName, "",
            IceStrength.AVERAGE, false,
            TarCreator.totalUnitsByStrength[IceStrength.AVERAGE]!!,
            defaultTimeHackerGroup(IceStrength.AVERAGE, 1, 1),
            defaultTimeHackerGroup(IceStrength.AVERAGE, 5, 1),
            defaultTimeHackerGroup(IceStrength.AVERAGE, 10, 5),
            null
        )

    constructor(id: String, toClone: TarIceLayer) :
        this(
            id, LayerType.TAR_ICE, toClone.level, toClone.name, toClone.note, toClone.strength, toClone.hacked,
            toClone.totalUnits, toClone.time1Level1Hacker, toClone.time1Level5Hacker, toClone.time5Level10Hackers, toClone.original,
        )

    constructor(id: String, level: Int, name: String, note: String, strength: IceStrength, original: IceLayer?) :
        this(
            id, LayerType.TAR_ICE, level, name, note, strength, false,
            TarCreator.totalUnitsByStrength[strength]!!,
            defaultTimeHackerGroup(strength, 1, 1),
            defaultTimeHackerGroup(strength, 5, 1),
            defaultTimeHackerGroup(strength, 10, 5),
            original
        )

    override fun updateInternal(key: String, value: String): Boolean {
        if (key == STRENGTH.name) {
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
