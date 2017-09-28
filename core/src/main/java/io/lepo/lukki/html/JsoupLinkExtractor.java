package io.lepo.lukki.html;

import io.lepo.lukki.core.CrawlContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

final class JsoupLinkExtractor implements BiFunction<CrawlContext, Document, String[]> {

  @Override
  public String[] apply(CrawlContext crawlContext, Document document) {
    // TODO: Link extractor shouldn't need to be aware of this.
    // TODO: Logging
    if (crawlContext.getUrl().startsWith(crawlContext.getOriginUrl())) {
      Elements anchorElements = document.select("a");
      List<String> links = new ArrayList<>(100);

      for (Element element : anchorElements) {
        links.add(element.absUrl("href"));
      }

      return links.toArray(new String[links.size()]);
    }

    return new String[0];
  }
}