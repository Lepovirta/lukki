package io.lepo.lukki.testserver

import jodd.http.HttpRequest
import jodd.http.HttpResponse
import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.util.*

@TestInstance(Lifecycle.PER_CLASS)
internal class TestServerTests {

    private val server = TestServer(0, PAGE_COUNT, null)

    @BeforeAll
    fun beforeAll() {
        server.start()
    }

    @AfterAll
    fun afterAll() {
        server.stop()
    }

    @Test
    @DisplayName("Test server should contain a root document")
    fun testRootShouldBeFound() {
        val response = getPath("/")
        assertOkResponse(response)
    }

    @Test
    @DisplayName("Unknown paths are responded to with 404")
    fun test404() {
        val response = getPath("/12345")
        Assertions.assertThat(response.statusCode()).isEqualTo(404)
        Assertions.assertThat(response.contentType()).contains("text/plain")
    }

    @Test
    @DisplayName("Test server should have all the paths reachable starting from root")
    fun testAllPathsReachable() {
        val pathsVisited = HashSet<String>(PAGE_COUNT)
        val pathsToProcess = LinkedList<String>()
        pathsToProcess.offer("/")

        while (!pathsToProcess.isEmpty()) {
            // Get the next path and its child links
            val path = pathsToProcess.poll()
            val response = getPath(path)
            assertOkResponse(response)
            val childLinks = extractLinks(response.bodyText())

            // Process the child links next
            val nextPaths = ArrayList(childLinks)
            nextPaths.removeAll(pathsVisited)
            pathsToProcess.addAll(nextPaths)

            // Update which paths have been visited
            pathsVisited.add(path)
            pathsVisited.addAll(childLinks)
        }

        Assertions.assertThat(pathsVisited).hasSize(PAGE_COUNT)
    }

    private fun getPath(path: String): HttpResponse {
        return HttpRequest.get("http://127.0.0.1:" + server.port + path).send()
    }

    private fun assertOkResponse(response: HttpResponse) {
        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        Assertions.assertThat(response.contentType()).isEqualTo("text/html")
    }

    private fun extractLinks(htmlString: String): List<String> {
        val doc = Jsoup.parse(htmlString)
        val anchors = doc.select("a")
        return anchors.map { it.attr("href") }
    }

    companion object {
        private val PAGE_COUNT = 10
    }
}
