package io.lepo.lukki.html;

import io.lepo.lukki.core.CrawlContext;
import io.lepo.lukki.core.Script;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JsoupLinkExtractor implements Script.LinkExtractor<Document> {

  private static final Logger log = LoggerFactory.getLogger(JsoupLinkExtractor.class);

  @Override
  public URI[] apply(CrawlContext crawlContext, Document document) {
    // TODO: extract links from places other than anchors
    // TODO: bubble invalid URIs as errors
    Elements anchorElements = document.select("a");
    List<URI> links = new ArrayList<>(100);

    for (Element element : anchorElements) {
      String href = element.absUrl("href");
      try {
        URI uri = URI.create(href);
        links.add(uri);
      } catch (IllegalArgumentException ex) {
        log.warn("Found invalid URI: {}", href);
      }
    }

    return links.toArray(new URI[links.size()]);
  }
}
