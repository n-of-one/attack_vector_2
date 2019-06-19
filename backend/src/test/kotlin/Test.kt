import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
import org.springframework.test.context.junit4.SpringRunner

/**
 *
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [AttackVector::class])
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

    @Before
    fun reset() {
        siteDataRepo.deleteAll()
        nodeRepo.deleteAll()
        siteId = siteRepo.createSite("siteId")
    }

    @Test
    fun addNode() {
        val layoutBefore = layoutRepo.findById(siteId).get()
        assertThat(layoutBefore.nodeIds.size, IsEqual(0))

        val command = AddNode("1", 10, 20, NodeType.PASSCODE_STORE)
        editorService.addNode(command)

        val layoutAfter = layoutRepo.findById(siteId).get()
        assertThat(layoutAfter.nodeIds.size, IsEqual(1))

        val node = nodeRepo.findById(layoutAfter.nodeIds[0]).get()
        assertThat(node.x, IsEqual(10))
        assertThat(node.y, IsEqual(20))
        assertThat(node.type, IsEqual(NodeType.PASSCODE_STORE))
    }


}