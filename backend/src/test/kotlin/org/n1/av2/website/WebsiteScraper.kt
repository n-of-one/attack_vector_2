package org.n1.av2.website

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File


data class Page(
    val path: String,
    val title: String,
    val text: String,
    val position: Int
)

// Use this to extract the entire docs, for reviewing by LLMs.
class WebsiteScraper {

    @Disabled
    @Test
    fun scrapeWebsite() {
        val root = File("../website/docs/")
        val pages = visitDirectory(root)
        printPagesInOrder(pages)
    }

    fun visitDirectory(dir: File): List<Page> {
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        val entries = mutableListOf<Pair<Int, File>>()

        // directories (categories)
        dir.listFiles()?.filter { it.isDirectory }?.forEach { sub ->
            val pos = readCategoryPosition(sub) ?: Int.MAX_VALUE
            entries += pos to sub
        }

        // markdown files
        dir.listFiles()?.filter { it.isFile && (it.extension == "md" || it.extension == "mdx") && it.name != "_category_.json" }?.forEach { f ->
            val pos = readFileSidebarPosition(f) ?: Int.MAX_VALUE
            entries += pos to f
        }

        val sorted = entries.sortedWith(compareBy<Pair<Int, File>>({ it.first }, { it.second.name }))

        val pages = mutableListOf<Page>()
        for ((_, file) in sorted) {
            if (file.isDirectory) {
                pages += visitDirectory(file)
            } else {
                val title = readPageTitle(file) ?: file.name
                val text = readPageContentWithoutFrontmatter(file)
                pages += Page(file.path.removePrefix("..\\website\\docs\\"), title, text, readFileSidebarPosition(file) ?: Int.MAX_VALUE)
            }
        }

        return pages
    }

    private fun readCategoryPosition(dir: File): Int? {
        val cat = File(dir, "_category_.json")
        if (!cat.exists()) return null
        val content = try { cat.readText() } catch (e: Exception) { return null }
        val m = Regex("""["']?position["']?\s*:\s*(\d+)""").find(content)
        return m?.groupValues?.get(1)?.toInt()
    }

    private fun readFileSidebarPosition(file: File): Int? {
        val fm = extractFrontMatter(file) ?: return null
        val m = Regex("""(?m)^\s*sidebar_position\s*:\s*(\d+)\s*$""").find(fm)
        return m?.groupValues?.get(1)?.toInt()
    }

    private fun readPageTitle(file: File): String? {
        val fm = extractFrontMatter(file)
        if (fm != null) {
            val m = Regex("""(?m)^\s*title\s*:\s*["']?(.*?)["']?\s*$""").find(fm)
            if (m != null) return m.groupValues[1].trim()
        }
        val content = try { file.readText() } catch (e: Exception) { return null }
        val m2 = Regex("""(?m)^\s*#\s+(.*)""").find(content)
        return m2?.groupValues?.get(1)?.trim()
    }

    private fun readPageContentWithoutFrontmatter(file: File): String {
        val content = try { file.readText() } catch (e: Exception) { return "" }
        val fmRegex = Regex("""(?s)\A---\s*\n(.*?)\n---\s*\n""")
        return fmRegex.replace(content, "").trim()
    }

    private fun extractFrontMatter(file: File): String? {
        val content = try { file.readText() } catch (e: Exception) { return null }
        val fmRegex = Regex("""(?s)\A---\s*\n(.*?)\n---\s*""")
        val m = fmRegex.find(content)
        return m?.groupValues?.get(1)
    }

    private fun printPagesInOrder(pages: List<Page>) {
        val sortedPages = pages.sortedWith { a, b -> customSorter(a, b) }

        println("Documentation structure order:")
        sortedPages.forEachIndexed { i, p ->
            println("${i} - ${p.path}")
        }
        println("\n------------------\n")

//        sortedPages.forEach { p ->
//            println("File: ${p.title}\n")
//            println(p.text)
//            println("\n---------\n")
//        }
    }

    // Order the directories in this order: docs, player, gm, organizer, installation
    private fun customSorter(a: Page, b: Page): Int {
        val sortOrder = listOf("docs", "player", "gm", "organizer", "installation")
        val aDir = a.path.substringBefore("\\")
        val bDir = b.path.substringBefore("\\")
        val aIndex = sortOrder.indexOf(aDir).let { if (it == -1) Int.MAX_VALUE else it }
        val bIndex = sortOrder.indexOf(bDir).let { if (it == -1) Int.MAX_VALUE else it }
        return aIndex - bIndex
    }
}


