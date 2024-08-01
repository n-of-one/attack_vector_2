package org.n1.av2.backend.service.run.terminal.scanning

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.n1.av2.run.scanning.BlockedPathException
import org.n1.av2.run.scanning.TraverseNode

class TraverseNodeTest {


    private val start = TraverseNode("start")
    private val a = TraverseNode("a")
    private val b = TraverseNode("b")
    private val c = TraverseNode("c")
    private val target = TraverseNode("target")

    private fun connect(one: TraverseNode, two: TraverseNode) {
        one.connections.add(two)
        two.connections.add(one)
    }


    /**
     * Diamond network
     *
     *        a
     *    /       \
     * start  |   target
     *    \       /
     *        b
     */
    private fun diamondSetup(): List<TraverseNode> {
        listOf(start, a, b, target).forEach {
            it.distance = null
            it.visited = false
            it.connections.clear()
        }

        connect(start, a)
        connect(start, b)

        connect(a, target)
        connect(b, target)

        connect(a, b)

        return listOf(start, a, b, target)
    }


    @Test
    fun `test Diamond fillDistanceFromHere`() {
        diamondSetup()
        start.fillDistanceFromHere(1)
        val distances = listOf(start, a, b, target).map { it.distance }
        assertEquals(listOf(1, 2, 2, 3), distances)
    }

    @Test
    fun `test Diamond CreatePath`() {
        diamondSetup()
        start.fillDistanceFromHere(1)
        val path = TraverseNode.createPath(start, target)
        assertEquals(listOf("start", "a", "target"), path)
    }

    @Test
    fun `test Diamond removeIceBlockedNodes start ice`() {
        val nodes = diamondSetup()
        start.unhackedIce = true
        TraverseNode.removeIceBlockedNodes(start, nodes)
        assertEquals(listOf("a", "b"), start.connections.map { it.nodeId })
        assertEquals(listOf("b", "start", "target"), a.connections.map { it.nodeId })
        assertEquals(listOf("a", "start", "target"), b.connections.map { it.nodeId })
        assertEquals(listOf("a", "b"), target.connections.map { it.nodeId })
    }

    @Test
    fun `test Diamond removeIceBlockedNodes intermediate ice`() {
        val nodes = diamondSetup()
        a.unhackedIce = true
        TraverseNode.removeIceBlockedNodes(target, nodes)
        assertEquals(listOf("b"), start.connections.map { it.nodeId })
        assertEquals(listOf("start", "target"), b.connections.map { it.nodeId })
        assertEquals(listOf("b"), target.connections.map { it.nodeId })
    }

    @Test
    fun `test Diamond removeIceBlockedNodes target ice`() {
        val nodes = diamondSetup()
        target.unhackedIce = true
        TraverseNode.removeIceBlockedNodes(target, nodes)
        assertEquals(listOf("a", "b"), start.connections.map { it.nodeId })
        assertEquals(listOf("b", "start", "target"), a.connections.map { it.nodeId })
        assertEquals(listOf("a", "start", "target"), b.connections.map { it.nodeId })
        assertEquals(listOf("a", "b"), target.connections.map { it.nodeId })
    }

    @Test
    fun `test Diamond createPath with blocking ice a`() {
        val nodes = diamondSetup()
        a.unhackedIce = true // force the path to go through b
        TraverseNode.removeIceBlockedNodes(target, nodes)
        start.fillDistanceFromHere(1)
        val path = TraverseNode.createPath(start, target)
        assertEquals(listOf("start", "b", "target"), path)
    }

    @Test
    fun `test Diamond createPath with blocking ice a and b`() {
        val nodes = diamondSetup()
        a.unhackedIce = true
        b.unhackedIce = true // there is not paht to target
        TraverseNode.removeIceBlockedNodes(target, nodes)
        start.fillDistanceFromHere(1)
        assertEquals(listOf(1, null, null, null), nodes.map { it.distance })
        assertThrows(BlockedPathException::class.java) {
            TraverseNode.createPath(start, target)
        }
    }

    @Test
    fun `test Diamond createPath with blocking ice target`() {
        val nodes = diamondSetup()
        target.unhackedIce = true // force the path to go through b
        TraverseNode.removeIceBlockedNodes(target, nodes)
        start.fillDistanceFromHere(1)
        val path = TraverseNode.createPath(start, target)
        assertEquals(listOf("start", "a", "target"), path)
    }

    @Test
    fun `test Diamond createPath to start with ice at start (scan blocked first node of site)`() {
        val nodes = diamondSetup()
        start.unhackedIce = true // force the path to go through b
        TraverseNode.removeIceBlockedNodes(start, nodes)
        start.fillDistanceFromHere(1)
        val path = TraverseNode.createPath(start, start)
        assertEquals(listOf("start"), path)
    }

    @Test
    fun `test Diamond createPath fails to target if start is blocked`() {
        val nodes = diamondSetup()
        start.unhackedIce = true // force the path to go through b
        TraverseNode.removeIceBlockedNodes(start, nodes)
        start.fillDistanceFromHere(1)
        assertThrows(BlockedPathException::class.java) {
            TraverseNode.createPath(start, target)
        }
    }

    @Test
    fun `test Diamond createPath fails to target if a and b are blocked`() {
        val nodes = diamondSetup()
        a.unhackedIce = true // force the path to go through b
        b.unhackedIce = true // force the path to go through b
        TraverseNode.removeIceBlockedNodes(start, nodes)
        start.fillDistanceFromHere(1)
        assertThrows(BlockedPathException::class.java) {
            TraverseNode.createPath(start, target)
        }
    }

    /**
     * Asymnmetric network
     *
     *       a - c
     *    /        \
     * start     target
     *    \        /
     *        b
     */
    private fun asymmetricSetup(): List<TraverseNode> {
        listOf(start, a, b, c, target).forEach {
            it.distance = null
            it.visited = false
            it.connections.clear()
        }

        connect(start, a)
        connect(start, b)

        connect(a, b)
        connect(a, c)

        connect(b, target)
        connect(c, target)

        return listOf(start, a, b, c, target)
    }

    @Test
    fun `test Asymmetric fillDistanceFromHere`() {
        asymmetricSetup()
        start.fillDistanceFromHere(1)
        val distances = listOf(start, a, b, c, target).map { it.distance }
        assertEquals(listOf(1, 2, 2, 3, 3), distances)
    }

    @Test
    fun `test Asymmetric fillDistanceFromHere ice blocking shortest path`() {
        val nodes = asymmetricSetup()
        b.unhackedIce = true // force the path to go through a and c
        TraverseNode.removeIceBlockedNodes(target, nodes)
        start.fillDistanceFromHere(1)
        val distances = listOf(start, a, b, c, target).map { it.distance }
        assertEquals(listOf(1, 2, null, 3, 4), distances)
    }

    @Test
    fun `test Asymmetric createPath`() {
        asymmetricSetup()
        start.fillDistanceFromHere(1)
        val path = TraverseNode.createPath(start, target)
        assertEquals(listOf("start", "b", "target"), path)
    }

    @Test
    fun `test Asymmetric createPath with ice blocking shortest path`() {
        val nodes = asymmetricSetup()
        b.unhackedIce = true // force the path to go through a and c
        TraverseNode.removeIceBlockedNodes(target, nodes)
        start.fillDistanceFromHere(1)
        val path = TraverseNode.createPath(start, target)
        assertEquals(listOf("start", "a", "c", "target"), path)
    }


    @Test
    fun `test Asymmetric network from start with none blocked`() {
        asymmetricSetup()
        val network = start.unblockedNetwork(emptyList()).map { it.nodeId }.sorted()
        assertEquals(listOf("start", "a", "b", "c", "target").sorted(), network)
    }


    @Test
    fun `test Asymmetric network from start with blocked a and b`() {
        asymmetricSetup()
        a.unhackedIce = true
        b.unhackedIce = true
        val network = start.unblockedNetwork(emptyList()).map { it.nodeId }.sorted()
        assertEquals(listOf("start", "a", "b").sorted(), network)
    }

    @Test
    fun `test Asymmetric network from start with blocked c and b`() {
        asymmetricSetup()
        c.unhackedIce = true
        b.unhackedIce = true
        val network = start.unblockedNetwork(emptyList()).map { it.nodeId }.sorted()
        assertEquals(listOf("start", "a", "b", "c").sorted(), network)
    }

    @Test
    fun `test Asymmetric network from start with blocked a`() {
        asymmetricSetup()
        a.unhackedIce = true
        val network = start.unblockedNetwork(emptyList()).map { it.nodeId }.sorted()
        assertEquals(listOf("start", "a", "b", "c", "target").sorted(), network)
    }


    @Test
    fun `test Asymmetric network from target with none blocked`() {
        asymmetricSetup()
        val network = target.unblockedNetwork(emptyList()).map { it.nodeId }.sorted()
        assertEquals(listOf("start", "a", "b", "c", "target").sorted(), network)
    }

    @Test
    fun `test Asymmetric network from target with blocked c and b`() {
        asymmetricSetup()
        c.unhackedIce = true
        b.unhackedIce = true
        val network = target.unblockedNetwork(emptyList()).map { it.nodeId }.sorted()
        assertEquals(listOf("target", "b", "c").sorted(), network)
    }
}
