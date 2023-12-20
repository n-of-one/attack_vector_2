package org.n1.av2.backend.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class FileNameUtilTest() {
    private val util = FileNameUtil()

    @ParameterizedTest()
    @MethodSource("fileNames")
    fun testMakeSafe(testCase: Pair<String, String>) {
        val (input, expected) = testCase
        val normalized = util.makeSafe(input)
        assertEquals(expected, normalized)
    }

    companion object {
        @JvmStatic
        fun fileNames() = listOf(
            Pair("\\test", "_test"),  // forward slash is removed
            Pair("t/est", "t_est"), // backward slash is removed
            Pair("te*st", "te_st"), // star is removed
            Pair("tes.t", "tes_t"), // dot is removed
            Pair("test<", "test_"), // lesser than is rmeoved
            Pair("test >", "test _"), // greater than is removed
            Pair("? test", "_ test"), // question mark is removed
            Pair(": test", "_ test"), // colon is removed
            Pair("; test", "_ test"), // semi-colon  is removed
            Pair("\" test", "_ test"), // double-quotes  is removed
            Pair("| test", "_ test"), // pipe  is removed
            Pair("\u0000test", "_test"), // non-printable character is removed
            Pair("\u202Etest", "_test"), // dangerous non-printable character is removed (reverse direction)

            Pair("test", "test"), // normal ascii is allowed
            Pair("৪୨", "৪୨"), // non-western digits are allowed
            Pair("안녕하세요", "안녕하세요"), // non ascii characters are allowed
            Pair("⇐", "⇐"), // symbols are allowed
            Pair("\uD83D\uDC31", "\uD83D\uDC31"), // emojii are allowed (cat)
        )

    }
}