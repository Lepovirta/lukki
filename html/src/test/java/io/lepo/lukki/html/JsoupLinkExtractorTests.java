package io.lepo.lukki.html;

import io.lepo.lukki.core.CrawlContext;
import io.lepo.lukki.core.CrawlJob;
import java.net.URI;
import java.nio.charset.Charset;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsoupLinkExtractorTests {

  private static final JsoupLinkExtractor extractor = new JsoupLinkExtractor();

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
    final Document doc = Jsoup.parse(htmlText, ctx.getUri().toString());

    final URI[] uris = extractor.apply(ctx, doc);

    Assertions.assertThat(uris).containsExactlyInAnyOrder(
        URI.create("http://localhost/hello"),
        URI.create("http://localhost/foo/bar"),
        URI.create("http://google.com")
    );
  }
}
