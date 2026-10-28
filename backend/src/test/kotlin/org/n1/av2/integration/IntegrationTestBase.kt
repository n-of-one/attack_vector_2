package org.n1.av2.integration

import com.mongodb.client.MongoClient
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.n1.av2.editor.SiteEditorStateRepo
import org.n1.av2.hacker.hacker.Hacker
import org.n1.av2.hacker.hacker.HackerRepo
import org.n1.av2.hacker.hackerstate.HackerStateRepo
import org.n1.av2.hacker.skill.SkillRepo
import org.n1.av2.integration.stomp.StompClientService
import org.n1.av2.layer.ice.netwalk.NetwalkIceStatusRepo
import org.n1.av2.layer.ice.password.IcePasswordStatusRepo
import org.n1.av2.layer.ice.sweeper.SweeperIceStatusRepo
import org.n1.av2.layer.ice.tangle.TangleIceStatusRepo
import org.n1.av2.layer.ice.tar.TarIceStatusRepo
import org.n1.av2.layer.ice.wordsearch.WordSearchIceStatusRepo
import org.n1.av2.layer.other.keystore.IcePasswordRepository
import org.n1.av2.layer.other.timeradjuster.TimerAdjusterStatusRepository
import org.n1.av2.platform.config.ConfigEntryRepo
import org.n1.av2.platform.db.DbSchemaVersionRepository
import org.n1.av2.platform.db.DbSchemaVersioning
import org.n1.av2.platform.iam.user.DefaultUserService
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityRepo
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.run.entity.RunRepo
import org.n1.av2.run.runlink.RunLinkRepo
import org.n1.av2.script.ScriptRepository
import org.n1.av2.script.access.ScriptAccessRepository
import org.n1.av2.script.credittransaction.CreditTransactionRepo
import org.n1.av2.script.income.ScriptIncomeDateRepository
import org.n1.av2.script.ram.RamRepository
import org.n1.av2.script.type.ScriptTypeRepository
import org.n1.av2.site.entity.ConnectionRepo
import org.n1.av2.site.entity.NodeRepo
import org.n1.av2.site.entity.SitePropertiesRepo
import org.n1.av2.statistics.IceHackStatisticRepo
import org.n1.av2.timer.TimerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

/**
 * Base class for integration tests that boot the full application without MongoDB.
 *
 * All Spring Data repositories are replaced by Mockito mocks, whose default answers
 * (empty lists / Optional.empty / null) act as an empty database. Services, security,
 * websockets and controllers all run for real. Database behavior itself is covered by
 * the Playwright e2e test suite, not by these tests.
 *
 * Every repository must be mocked here: a repository left real would try to bootstrap
 * a MongoRepositoryFactory against the mocked MongoTemplate and fail at startup.
 * That failure mode is the safety net for repositories added in the future.
 *
 * Stubbing must happen in @BeforeEach, not @BeforeAll: @MockitoBean mocks are reset
 * after each test method.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

    // Mongo infrastructure: mocking the MongoClient prevents driver connection threads,
    // mocking the MongoTemplate prevents eager index creation (autoIndexCreation=true) over TCP.
    @MockitoBean
    protected lateinit var mongoClient: MongoClient

    @MockitoBean
    protected lateinit var mongoTemplate: MongoTemplate

    // Boot's Data Mongo autoconfiguration would otherwise build this from the mocked
    // MongoTemplate's converter (null) and fail.
    @MockitoBean
    protected lateinit var gridFsTemplate: GridFsTemplate

    // Startup listeners that cannot run against an empty mocked database:
    // DbSchemaVersioning would run all migrations, DefaultUserService would seed users.
    @MockitoBean
    protected lateinit var dbSchemaVersioning: DbSchemaVersioning

    @MockitoBean
    protected lateinit var defaultUserService: DefaultUserService

    // All repositories.
    @MockitoBean
    protected lateinit var userEntityRepo: UserEntityRepo

    @MockitoBean
    protected lateinit var hackerRepo: HackerRepo

    @MockitoBean
    protected lateinit var skillRepo: SkillRepo

    @MockitoBean
    protected lateinit var hackerStateRepo: HackerStateRepo

    @MockitoBean
    protected lateinit var configEntryRepo: ConfigEntryRepo

    @MockitoBean
    protected lateinit var runLinkRepo: RunLinkRepo

    @MockitoBean
    protected lateinit var runRepo: RunRepo

    @MockitoBean
    protected lateinit var scriptRepository: ScriptRepository

    @MockitoBean
    protected lateinit var scriptAccessRepository: ScriptAccessRepository

    @MockitoBean
    protected lateinit var scriptTypeRepository: ScriptTypeRepository

    @MockitoBean
    protected lateinit var scriptIncomeDateRepository: ScriptIncomeDateRepository

    @MockitoBean
    protected lateinit var creditTransactionRepo: CreditTransactionRepo

    @MockitoBean
    protected lateinit var ramRepository: RamRepository

    @MockitoBean
    protected lateinit var timerRepository: TimerRepository

    @MockitoBean
    protected lateinit var sitePropertiesRepo: SitePropertiesRepo

    @MockitoBean
    protected lateinit var nodeRepo: NodeRepo

    @MockitoBean
    protected lateinit var connectionRepo: ConnectionRepo

    @MockitoBean
    protected lateinit var siteEditorStateRepo: SiteEditorStateRepo

    @MockitoBean
    protected lateinit var dbSchemaVersionRepository: DbSchemaVersionRepository

    @MockitoBean
    protected lateinit var icePasswordRepository: IcePasswordRepository

    @MockitoBean
    protected lateinit var icePasswordStatusRepo: IcePasswordStatusRepo

    @MockitoBean
    protected lateinit var netwalkIceStatusRepo: NetwalkIceStatusRepo

    @MockitoBean
    protected lateinit var tangleIceStatusRepo: TangleIceStatusRepo

    @MockitoBean
    protected lateinit var wordSearchIceStatusRepo: WordSearchIceStatusRepo

    @MockitoBean
    protected lateinit var sweeperIceStatusRepo: SweeperIceStatusRepo

    @MockitoBean
    protected lateinit var tarIceStatusRepo: TarIceStatusRepo

    @MockitoBean
    protected lateinit var timerAdjusterStatusRepository: TimerAdjusterStatusRepository

    @MockitoBean
    protected lateinit var iceHackStatisticRepo: IceHackStatisticRepo

    @Autowired
    protected lateinit var stompClientService: StompClientService

    @LocalServerPort
    private val port = 0

    // Canned users, replacing the seeding that DefaultUserService would have done.
    protected val gmUser = UserEntity(id = "user-gm-test", name = "gm", type = UserType.GM)
    protected val hackerUser = UserEntity(id = "user-hacker-test", name = "hacker", type = UserType.HACKER)
    protected val stalkerUser = UserEntity(id = "user-stalker-test", name = "Stalker", type = UserType.HACKER)

    @BeforeAll
    fun setupPort() {
        stompClientService.port = port
    }

    @BeforeEach
    fun stubCannedData() {
        listOf(gmUser, hackerUser, stalkerUser).forEach { user ->
            given(userEntityRepo.findByNameIgnoreCase(user.name)).willReturn(user)
            given(userEntityRepo.findById(user.id)).willReturn(Optional.of(user))
        }
        listOf(hackerUser, stalkerUser).forEach { user ->
            given(hackerRepo.findByHackerUserId(user.id))
                .willReturn(Hacker(user.id.replace("user", "hacker"), user.id, HackerIcon.CAT, user.name))
        }

        // save() must echo its argument on repos where the return value is consumed by non-null Kotlin code.
        given(userEntityRepo.save(any())).willAnswer { it.getArgument(0) }
        given(hackerStateRepo.save(any())).willAnswer { it.getArgument(0) }
        given(ramRepository.save(any())).willAnswer { it.getArgument(0) }
        given(sitePropertiesRepo.save(any())).willAnswer { it.getArgument(0) }
    }
}
