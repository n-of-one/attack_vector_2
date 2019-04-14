import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.n1.mainframe.backend.AttackVector
import org.n1.mainframe.backend.model.site.Site
import org.n1.mainframe.backend.model.site.enums.NodeType
import org.n1.mainframe.backend.model.ui.AddNode
import org.n1.mainframe.backend.repo.NodeRepo
import org.n1.mainframe.backend.repo.SiteRepo
import org.n1.mainframe.backend.service.EditorService
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
    lateinit var siteRepo: SiteRepo
    @Autowired
    lateinit var nodeRepo: NodeRepo

    @Before
    fun reset() {
        siteRepo.deleteAll()
        nodeRepo.deleteAll()
        siteRepo.save(Site("1", "siteId"))

    }

    @Test
    fun addNode() {
        val siteBefore = siteRepo.findById("1").get()
        assertThat(siteBefore.nodeIds.size, IsEqual(0))

        val command = AddNode("1", 10, 20, NodeType.PASSCODE_STORE)
        editorService.addNode(command)

        val siteAfter = siteRepo.findById("1").get()
        assertThat(siteAfter.nodeIds.size, IsEqual(1))

        val node = nodeRepo.findById(siteAfter.nodeIds[0]).get()
        assertThat(node.x, IsEqual(10))
        assertThat(node.y, IsEqual(20))
        assertThat(node.type, IsEqual(NodeType.PASSCODE_STORE))
    }


}