package io.lepo.lukki.html;

import io.lepo.lukki.core.AssertionResult;
import io.lepo.lukki.core.CrawlContext;
import io.lepo.lukki.core.Script;
import java.io.InputStream;
import java.util.List;
import java.util.function.BiFunction;
import org.jsoup.nodes.Document;

public class Html {

  public static final BiFunction<CrawlContext, InputStream, Document> mapper =
      new JsoupDocumentParser();

  public static final BiFunction<CrawlContext, Document, String[]> linkExtractor =
      new JsoupLinkExtractor();

  public static final String mimeType = "text/html";

  public static Script<Document> script(
      List<BiFunction<CrawlContext, Document, AssertionResult>> assertionFunctions
  ) {
    return new Script<>(mapper, linkExtractor, assertionFunctions);
  }

  private Html() {
  }
}
