package io.lepo.lukki.testserver;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageHtmlFormatterTests {

  @Test
  @DisplayName("Generated HTML should contain the given links")
  void testLinksInHtml() {
    String[] links = {"/foo", "/bar", "/baz"};

    String html = PageHtmlFormatter.format("/mypage", Arrays.asList(links));
    Document htmlDoc = Jsoup.parse(html);
    Stream<String> hrefs = htmlDoc.select("a").stream().map((a) -> a.attr("href"));

    Assertions.assertThat(hrefs).containsExactly(links);
  }

  @Test
  @DisplayName("Generated HTML should contain the page link")
  void testPageLinkInHtml() {
    String pageLink = "/mypage";
    String html = PageHtmlFormatter.format(pageLink, Collections.emptyList());

    Document htmlDoc = Jsoup.parse(html);

    Assertions.assertThat(htmlDoc.title()).isEqualTo(pageLink);
  }
}
