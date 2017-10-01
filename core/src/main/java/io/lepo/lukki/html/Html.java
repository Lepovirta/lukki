package io.lepo.lukki.html;

import io.lepo.lukki.core.Script;
import java.util.List;
import org.jsoup.nodes.Document;

public class Html {

  public static final Script.EntityParser<Document> mapper =
      new JsoupDocumentParser();

  public static final Script.LinkExtractor<Document> linkExtractor =
      new JsoupLinkExtractor();

  public static final String mimeType = "text/html";

  public static Script<Document> script(
      List<Script.AssertionFunction<Document>> assertionFunctions
  ) {
    return new Script<>(mapper, linkExtractor, assertionFunctions);
  }

  private Html() {
  }
}
