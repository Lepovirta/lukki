package io.lepo.lukki.testserver

import java.util.stream.Collectors

internal object PageHtmlFormatter {

    fun format(pageLink: String, links: List<String>): String {
        val linksHtml = formatLinks(links)
        val headerHtml = String.format("<head><title>%s</title></head>", pageLink)
        return arrayOf(
                "<html>",
                headerHtml,
                "<body><ul>",
                linksHtml,
                "</ul></body>",
                "</html>"
        ).joinToString("\n")
    }

    private fun formatLinks(links: List<String>): String =
            links.stream()
                    .map { String.format("<li><a href=\"%s\">%s</a></li>", it, it) }
                    .collect(Collectors.joining("\n", "<ul>", "</ul>"))
}
