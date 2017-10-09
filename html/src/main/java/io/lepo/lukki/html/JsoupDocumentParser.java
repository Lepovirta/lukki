package io.lepo.lukki.html;

import io.lepo.lukki.core.CrawlContext;
import io.lepo.lukki.core.Script;
import java.io.IOException;
import java.io.InputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JsoupDocumentParser implements Script.DocumentParser<Document> {

  private static final Logger log = LoggerFactory.getLogger(JsoupDocumentParser.class);

  @Override
  public Document apply(CrawlContext crawlContext, InputStream input) {
    try {
      log.debug("Parsing document from URL [{}] with JSOUP", crawlContext.getUri());
      return Jsoup.parse(input, crawlContext.getCharset().name(), crawlContext.getUri().toString());
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
