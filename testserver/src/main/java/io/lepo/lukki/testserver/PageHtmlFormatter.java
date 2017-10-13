package io.lepo.lukki.testserver;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class PageHtmlFormatter {

  private PageHtmlFormatter() {
  }

  static String format(String pageLink, List<String> links) {
    String linksHtml = formatLinks(links);
    String headerHtml = String.format("<head><title>%s</title></head>", pageLink);
    return Stream.of(
        "<html>",
        headerHtml,
        "<body><ul>",
        linksHtml,
        "</ul></body>",
        "</html>"
    ).collect(Collectors.joining("\n"));
  }

  private static String formatLinks(List<String> links) {
    return links.stream()
        .map(link -> String.format("<li><a href=\"%s\">%s</a></li>", link, link))
        .collect(Collectors.joining("\n", "<ul>", "</ul>"));
  }
}
