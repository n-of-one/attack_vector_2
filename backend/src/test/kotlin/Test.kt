import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.n1.av2.backend.AttackVector
import org.n1.av2.backend.model.db.site.enums.NodeType
import org.n1.av2.backend.model.ui.AddNode
import org.n1.av2.backend.repo.LayoutRepo
import org.n1.av2.backend.repo.NodeRepo
import org.n1.av2.backend.repo.SiteDataRepo
import org.n1.av2.backend.service.EditorService
import org.n1.av2.backend.service.site.SiteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 *
 */
@SpringBootTest(
    classes = arrayOf(AttackVector::class)
)
class Test {

    @Autowired
    lateinit var editorService: EditorService
    @Autowired
    lateinit var siteDataRepo: SiteDataRepo
    @Autowired
    lateinit var nodeRepo: NodeRepo
    @Autowired
    lateinit var layoutRepo: LayoutRepo
    @Autowired
    lateinit var siteRepo: SiteService

    var siteId: String = ""

    @BeforeAll
    fun reset() {
        siteDataRepo.deleteAll()
        nodeRepo.deleteAll()
        siteId = siteRepo.createSite("siteId")
    }

    @Test
    fun addNode() {
        val layoutBefore = layoutRepo.findById(siteId).get()

        assertEquals(0, layoutBefore.nodeIds.size)

        val command = AddNode("1", 10, 20, NodeType.PASSCODE_STORE)
        editorService.addNode(command)

        val layoutAfter = layoutRepo.findById(siteId).get()
        assertEquals(1, layoutAfter.nodeIds.size)

        val node = nodeRepo.findById(layoutAfter.nodeIds[0]).get()
        assertEquals(10, node.x)
        assertEquals(20, node.y)
        assertEquals(NodeType.PASSCODE_STORE, node.type)
    }


}