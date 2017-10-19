package io.lepo.lukki.testserver

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.*
import java.util.regex.Pattern

internal class PageGeneratorTests {

    private val validPathPattern = Pattern.compile("^/[a-z/]*$")
    private val testCounts = intArrayOf(11, 102, 324)

    @Test
    @DisplayName("Correct amount of pages are generated")
    fun testPageCount() {
        for (i in testCounts) {
            Assertions
                    .assertThat(generatePages(i).size)
                    .isEqualTo(i)
        }
    }

    @Test
    @DisplayName("Generated pages contains root page")
    fun testContainsRootPage() {
        for (i in testCounts) {
            Assertions.assertThat(generatePages(i)).containsKey("/")
        }
    }

    @RepeatedTest(5)
    @DisplayName("All paths are in valid format")
    fun testLinkFormat() {
        val pages = generatePages(100)
        for ((key, value) in pages) {
            Assertions.assertThat(key).matches(validPathPattern)
            for (path in value) {
                Assertions.assertThat(path).matches(validPathPattern)
            }
        }
    }

    @RepeatedTest(20)
    @DisplayName("All paths are reachable starting from root")
    fun testAllPathsReachable() {
        val pages = generatePages(10)
        val pathsVisited = HashSet<String>(10)
        val pathsToProcess = LinkedList<String>()
        pathsToProcess.offer("/") // Start from root

        while (!pathsToProcess.isEmpty()) {
            // Get the next path and its child links
            val path = pathsToProcess.poll()
            val childLinks = pages[path]

            if (childLinks == null) {
                fail<Any>("No child links found for path: " + path)
            }

            // Process the child links next
            val nextPaths = ArrayList(childLinks!!)
            nextPaths.removeAll(pathsVisited)
            pathsToProcess.addAll(nextPaths)

            // Update which paths have been visited
            pathsVisited.add(path)
            pathsVisited.addAll(childLinks)
        }

        Assertions.assertThat(pathsVisited).containsAll(pages.keys)
    }

    private fun generatePages(validPagesCount: Int): Map<String, List<String>> {
        return PageGenerator().generate(validPagesCount)
    }
}
