package io.lepo.lukki.html;

import io.lepo.lukki.core.CrawlContext;
import io.lepo.lukki.core.Script;
import java.io.IOException;
import java.io.InputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

final class JsoupDocumentParser implements Script.EntityParser<Document> {

  @Override
  public Document apply(CrawlContext crawlContext, InputStream input) {
    // TODO: Logging
    try {
      return Jsoup.parse(input, null, crawlContext.getUrl());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
