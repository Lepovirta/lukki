package io.lepo.lukki.testserver

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

internal class PageHtmlFormatterTests {

    @Test
    @DisplayName("Generated HTML should contain the given links")
    fun testLinksInHtml() {
        val links = arrayOf("/foo", "/bar", "/baz")

        val html = PageHtmlFormatter.format("/mypage", Arrays.asList(*links))
        val htmlDoc = Jsoup.parse(html)
        val hrefs = htmlDoc.select("a").map { it.attr("href") }

        Assertions.assertThat(hrefs).containsExactly(*links)
    }

    @Test
    @DisplayName("Generated HTML should contain the page link")
    fun testPageLinkInHtml() {
        val pageLink = "/mypage"
        val html = PageHtmlFormatter.format(pageLink, emptyList())

        val htmlDoc = Jsoup.parse(html)

        Assertions.assertThat(htmlDoc.title()).isEqualTo(pageLink)
    }
}
