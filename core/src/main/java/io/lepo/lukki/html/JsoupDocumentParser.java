package io.lepo.lukki.html;

import io.lepo.lukki.core.CrawlContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

final class JsoupDocumentParser implements BiFunction<CrawlContext, InputStream, Document> {

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
