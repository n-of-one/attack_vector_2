package org.n1.av2.backend.service.layer.ice.password

import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

class TangleUtilTest {

    @Test
    fun `Test intersect 1`() {
        testIntersect(1.0F, 0.0F, 0.0F, 1.0F, 1F, 1F)
    }

    @Test
    fun `Test intersect 2`() {
        testIntersect(2.0F, 0.0F, -0.5F, 5.0F, 2F, 4F)
    }


    @Test
    fun `Test toLine (0,0)-(1,1)`() {
        val line = toLine( 0F, 0F, 1F, 1F)
        assertThat(line.slope, Is(1F))
        assertThat(line.yOffset, Is(0F))
    }

    @Test
    fun `Test toLine (1,1)-(0,0)`() {
        val line = toLine( 1F, 1F, 0F, 0F)
        assertThat(line.slope, Is(1F))
        assertThat(line.yOffset, Is(0F))
    }

    @Test
    fun `Test toLine (6,2) (2,4)`() {
        val line = toLine( 6F, 2F, 2F, 4F)
        assertThat(line.slope, Is(-0.5F))
        assertThat(line.yOffset, Is(5F))
    }

    @Test
    fun `Test toLine (2,4) (6,2)`() {
        val line = toLine( 2F, 4F, 6F, 2F)
        assertThat(line.slope, Is(-0.5F))
        assertThat(line.yOffset, Is(5F))
    }

    private fun testIntersect(slope1: Float, yOffset1: Float, slope2: Float, yOffset2: Float, expectedX: Float, expectedY: Float) {
        val line1 = TLine(slope1, yOffset1)
        val line2 = TLine(slope2, yOffset2)

        val intersection = intersect(line1, line2)
        assertThat(intersection.x, Is(expectedX))
        assertThat(intersection.y, Is(expectedY))

    }

    @Test
    fun `Test segmentsIntersect (0,-1)-(0,1) x (-1,0) x (1,0)`() {
        val answer = segmentsIntersect(
                0, -1, 0, 1,
                -1, 0, 1, 0)

        assertThat (answer, Is(true))
    }

    @Test
    fun `Test segmentsIntersect (0,-1)-(0,1) x (0,0) x (1,0)`() {
        val answer = segmentsIntersect(
                0, -1, 0, 1,
                0, 0, 1, 0)

        assertThat (answer, Is(true))
    }

    @Test
    fun `Test segmentsIntersect (0,-1)-(0,1) x (2,0) x (1,0)`() {
        val answer = segmentsIntersect(
                0, -1, 0, 1,
                2, 0, 1, 0)

        assertThat (answer, Is(false))
    }

    @Test
    fun `Test segmentsIntersect (2,1)-(6,3) x (-1,4) x (3,0)`() {
        val answer = segmentsIntersect(
                2, 1, 6, 3,
                -1, 4, 3, 0)

        assertThat (answer, Is(true))
    }

    @Test
    fun `Test segmentsIntersect (2,1)-(6,3) x (-1,4) x (1,2)`() {
        val answer = segmentsIntersect(
                2, 1, 6, 3,
                -1, 4, 1, 2)

        assertThat (answer, Is(false))
    }

    @Test
    fun `Test segmentsIntersect parallel horizontal (0,0)-(1,0) x (0,2) x (1,2)`() {
        val answer = segmentsIntersect(
                0, 0, 1, 0,
                0, 2, 1, 2)

        assertThat (answer, Is(false))
    }

    @Test
    fun `Test segmentsIntersect parallel vertical (0,0)-(0,1) x (2,0) x (2,1)`() {
        val answer = segmentsIntersect(
                0, 0, 0, 1,
                2, 0, 2, 1)

        assertThat (answer, Is(false))
    }
}