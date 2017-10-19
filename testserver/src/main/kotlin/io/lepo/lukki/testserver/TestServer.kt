package io.lepo.lukki.testserver

import io.javalin.Context
import io.javalin.Javalin
import org.slf4j.LoggerFactory
import kotlin.collections.Map.Entry

class TestServer @JvmOverloads constructor(
        port: Int,
        val pageCount: Int, seed: Long? = null
) {
    private val pageGenerator: PageGenerator = PageGenerator(seed)
    private val server: Javalin = Javalin
            .create()
            .port(port)
            .get("*") { handleGet(it) }
    private val pages: Map<String, String> by lazy {
        log.info("Generating pages")
        pageGenerator
                .generate(pageCount)
                .entries
                .associateBy({ it.key }) { formatPage(it) }
    }

    val port: Int
        get() = server.port()

    val seed: Long?
        get() = pageGenerator.seed

    private fun handleGet(ctx: Context) {
        log.debug("GET: {}", ctx.path())
        val doc = pages[ctx.path()]
        if (doc == null) {
            ctx.result("text/plain")
            ctx.result("Not found")
            ctx.status(404)
        } else {
            ctx.contentType("text/html")
            ctx.result(doc)
            ctx.status(200)
        }
    }

    fun start() {
        log.info(
                "Pages generated. Starting HTTP server on port [{}] with seed [{}].",
                server.port(),
                pageGenerator.seed
        )
        server.start()
    }

    private fun formatPage(page: Entry<String, List<String>>): String {
        return PageHtmlFormatter.format(page.key, page.value)
    }

    fun stop() {
        server.stop()
    }

    companion object {
        private val log = LoggerFactory.getLogger(TestServer::class.java)
    }
}
