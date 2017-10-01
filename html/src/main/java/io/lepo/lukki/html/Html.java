package io.lepo.lukki.html;

import io.lepo.lukki.core.Script;
import io.lepo.lukki.core.Script.DocumentParser;
import java.util.List;
import org.jsoup.nodes.Document;

public class Html {

  public static final DocumentParser<Document> parser =
      new JsoupDocumentParser();

  public static final Script.LinkExtractor<Document> linkExtractor =
      new JsoupLinkExtractor();

  public static final String mimeType = "text/html";

  public static Script<Document> script(
      List<Script.AssertionFunction<Document>> assertionFunctions
  ) {
    return new Script<>(parser, linkExtractor, assertionFunctions);
  }

  private Html() {
  }
}
