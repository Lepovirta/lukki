package io.lepo.lukki.core;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class Script<DocT> {

  public static final Script<Object> rejectAll = new Script<>(
      DocumentParser.rejectAll,
      LinkExtractor.noLinks,
      new ArrayList<>()
  );
  public static final Script<Object> acceptAll = new Script<>(
      DocumentParser.emptyParser,
      LinkExtractor.noLinks,
      new ArrayList<>()
  );
  private final DocumentParser<DocT> parser;
  private final LinkExtractor<DocT> linkExtractor;
  private final List<AssertionFunction<DocT>> assertionFunctions;

  public Script(
      DocumentParser<DocT> parser,
      LinkExtractor<DocT> linkExtractor,
      List<AssertionFunction<DocT>> assertionFunctions
  ) {
    this.parser = parser;
    this.linkExtractor = linkExtractor;
    this.assertionFunctions = assertionFunctions;
  }

  public Result run(CrawlContext context, InputStream input) {
    DocT doc = parser.apply(context, input);
    URI[] links = linkExtractor.apply(context, doc);

    AssertionResult[] assertionResults = new AssertionResult[assertionFunctions.size()];

    for (int i = 0; i < assertionFunctions.size(); i++) {
      AssertionResult assertionResult = assertionFunctions.get(i).apply(context, doc);
      assertionResults[i] = assertionResult;
    }

    return new Result(links, assertionResults);
  }

  public interface DocumentParser<DocT> extends BiFunction<CrawlContext, InputStream, DocT> {

    DocumentParser<Object> rejectAll = (context, input) -> {
      throw new UnsupportedOperationException(
          "No script found for mime type: " + context.getMimeType());
    };
    DocumentParser<Object> emptyParser = (context, input) -> new Object();
  }

  public interface LinkExtractor<DocT>
      extends BiFunction<CrawlContext, DocT, URI[]> {

    LinkExtractor<Object> noLinks = (context, doc) -> new URI[0];
  }

  public interface AssertionFunction<DocT>
      extends BiFunction<CrawlContext, DocT, AssertionResult> {

  }

  public static final class Result {

    public static final Result empty = new Result(new URI[0], new AssertionResult[0]);

    private final URI[] links;
    private final AssertionResult[] assertionResults;

    public Result(
        URI[] links,
        AssertionResult[] assertionResults
    ) {
      this.links = links;
      this.assertionResults = assertionResults;
    }

    public URI[] getLinks() {
      return links;
    }

    public AssertionResult[] getAssertionResults() {
      return assertionResults;
    }
  }
}
