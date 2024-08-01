package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.n1.av2.editor.SiteEditorState
import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.netwalk.NetwalkIceLayer
import org.n1.av2.layer.ice.netwalk.NetwalkIceStatus
import org.n1.av2.layer.ice.password.IcePasswordStatus
import org.n1.av2.layer.ice.password.PasswordIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceStatus
import org.n1.av2.layer.ice.tangle.TangleIceLayer
import org.n1.av2.layer.ice.tangle.TangleIceStatus
import org.n1.av2.layer.ice.tar.TarIceLayer
import org.n1.av2.layer.ice.tar.TarIceStatus
import org.n1.av2.layer.ice.wordsearch.WordSearchIceLayer
import org.n1.av2.layer.ice.wordsearch.WordSearchIceStatus
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.keystore.IcePassword
import org.n1.av2.layer.other.keystore.KeyStoreLayer
import org.n1.av2.layer.other.os.OsLayer
import org.n1.av2.layer.other.text.TextLayer
import org.n1.av2.layer.other.tripwire.Timer
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.db.DbSchemaVersion
import org.n1.av2.platform.db.MigrationStep
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.run.entity.Run
import org.n1.av2.run.runlink.RunLink
import org.n1.av2.site.entity.Connection
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.SiteProperties
import org.springframework.stereotype.Component

@Component
class V4_PackageRefactor : MigrationStep {

    override
    fun version() = 4

    override
    fun migrate(db: MongoDatabase): String {

        val collectionNames = db.listCollectionNames()

        collectionNames.forEach { collectionName ->
            val collection = db.getCollection(collectionName)

            if (collectionName == "hackerState" // hackerState will be recreated
                || collectionName == "userIceHackingState" // userIceHackingState is obsolete
                ) {

                collection.deleteMany(Document())
                return@forEach
            }

            if (collectionName == "dbSchemaVersion" || collectionName == "runLink") {
                upgradeWithObjectIds(collection)
            } else {
                upgradeWithStringIds(collection)
            }
        }

        return "Updated class to correspond to the new package structure"
    }

    private fun upgradeWithObjectIds(collection: MongoCollection<Document>) {
        val objectIds = collection.find().map { it.getObjectId("_id") }
        objectIds.filterNotNull().forEach { id ->
            val byId = Document("_id", id)
            val document:Document = collection.find(byId).first() ?: error("Document not found for id $id")
            val replacementJson = replacePackageNames(document.toJson())
            collection.replaceOne(byId, Document.parse(replacementJson))
        }
    }

    private fun upgradeWithStringIds(collection: MongoCollection<Document>) {
        val stringIds = collection.find().map { it.getString("_id") }
        stringIds.filterNotNull().forEach { id ->
            val byId = Document("_id", id)

            val document:Document = collection.find(byId).first() ?: error("Document not found for id $id")
            val replacementJson = replacePackageNames(document.toJson())
            collection.replaceOne(byId, Document.parse(replacementJson))
        }
    }

    private fun replacePackageNames(v3Json: String): String {

        val migrated = v3Json
            .replace("org.n1.av2.backend.entity.util.DbSchemaVersion", DbSchemaVersion::class.qualifiedName!!)

            .replace("org.n1.av2.backend.entity.site.Connection", Connection::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.SiteEditorState", SiteEditorState::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.SiteProperties", SiteProperties::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.Node", Node::class.qualifiedName!!)

            .replace("org.n1.av2.backend.entity.site.layer.OsLayer", OsLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.other.TextLayer", TextLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.other.CoreLayer", CoreLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.other.KeyStoreLayer", KeyStoreLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.other.TripwireLayer", TripwireLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.other.StatusLightLayer", StatusLightLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.ice.IceLayer", IceLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.ice.PasswordIceLayer", PasswordIceLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.ice.NetwalkIceLayer", NetwalkIceLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.ice.TangleIceLayer", TangleIceLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.ice.TarIceLayer", TarIceLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.ice.WordSearchIceLayer", WordSearchIceLayer::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.site.layer.ice.SweeperIceLayer", SweeperIceLayer::class.qualifiedName!!)

            .replace("org.n1.av2.backend.entity.run.Run", Run::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.run.RunLink", RunLink::class.qualifiedName!!)

            .replace("org.n1.av2.backend.entity.user.UserEntity", UserEntity::class.qualifiedName!!)

            .replace("org.n1.av2.backend.entity.ice.IcePasswordStatus", IcePasswordStatus::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.ice.NetwalkIceStatus", NetwalkIceStatus::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.ice.TangleIceStatus", TangleIceStatus::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.ice.TarIceStatus", TarIceStatus::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.ice.WordSearchIceStatus", WordSearchIceStatus::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.ice.SweeperIceStatus", SweeperIceStatus::class.qualifiedName!!)

            .replace("org.n1.av2.backend.entity.service.IcePassword", IcePassword::class.qualifiedName!!)
            .replace("org.n1.av2.backend.entity.service.Timer", Timer::class.qualifiedName!!)

        if (migrated.contains("org.n1.av2.backend")) error("Migration failed: ${migrated} still contains a reference to the previous package structure: \"org.n1.av2.backend\"" )

        return migrated
    }
}
