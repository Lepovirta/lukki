package io.lepo.lukki.html;

import io.lepo.lukki.core.CrawlContext;
import io.lepo.lukki.core.CrawlJob;
import io.lepo.lukki.core.Script;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HtmlTests {

  private static CrawlContext createContext(final URI uri) throws Exception {
    return new CrawlContext(
        CrawlJob.withHostsStartingWithUrlHost(uri),
        uri,
        "text/html",
        Charset.defaultCharset()
    );
  }

  private static String wrapHtmlBody(String htmlBody) {
    return String.join("\n",
        "<html>",
        "<head><title>hello</title>",
        "<body>",
        htmlBody,
        "</body></html>"
    );
  }

  private static String anchor(String link, String text) {
    return String.format("<a href=\"%s\">%s</a>", link, text);
  }

  @Test
  @DisplayName("Link extractor picks up links from anchors")
  void testLinkAnchorExtraction() throws Exception {
    final String htmlText = wrapHtmlBody(
        String.join("\n",
            anchor("/hello", "hello"),
            anchor("/foo/bar", "foobar"),
            anchor("http://google.com", "google")
        )
    );
    final CrawlContext ctx = createContext(URI.create("http://localhost"));

    final Script.Result result = Html
        .script(Collections.emptyList())
        .run(ctx, new ByteArrayInputStream(htmlText.getBytes()));

    Assertions.assertThat(result.getLinks()).containsExactlyInAnyOrder(
        URI.create("http://localhost/hello"),
        URI.create("http://localhost/foo/bar"),
        URI.create("http://google.com")
    );

    Assertions.assertThat(result.getAssertionResults()).isEmpty();
  }
}
