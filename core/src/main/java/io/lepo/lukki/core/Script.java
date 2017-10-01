package io.lepo.lukki.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class Script<Doc> {

  public interface DocumentParser<Entity> extends BiFunction<CrawlContext, InputStream, Entity> {

  }

  public interface LinkExtractor<Entity> extends BiFunction<CrawlContext, Entity, String[]> {

  }

  public interface AssertionFunction<Entity> extends
      BiFunction<CrawlContext, Entity, AssertionResult> {

  }

  public static final class Result {

    public static final Result empty = new Result(new String[]{}, new AssertionResult[]{});

    private final String[] links;
    private final AssertionResult[] assertionResults;

    public Result(
        String[] links,
        AssertionResult[] assertionResults
    ) {
      this.links = links;
      this.assertionResults = assertionResults;
    }

    public String[] getLinks() {
      return links;
    }

    public AssertionResult[] getAssertionResults() {
      return assertionResults;
    }
  }

  public static final Script<Object> throwOnEverything = new Script<>(
      (context, input) -> {
        throw new UnsupportedOperationException(
            "No script found for mime type: " + context.getMimeType());
      },
      (context, entity) -> new String[0],
      new ArrayList<>()
  );

  public static final Script<Object> acceptEverything = new Script<>(
      (context, input) -> new Object(),
      (context, entity) -> new String[0],
      new ArrayList<>()
  );

  private final DocumentParser<Doc> parser;
  private final LinkExtractor<Doc> linkExtractor;
  private final List<AssertionFunction<Doc>> assertionFunctions;

  public Script(
      DocumentParser<Doc> parser,
      LinkExtractor<Doc> linkExtractor,
      List<AssertionFunction<Doc>> assertionFunctions
  ) {
    this.parser = parser;
    this.linkExtractor = linkExtractor;
    this.assertionFunctions = assertionFunctions;
  }

  public Result run(CrawlContext context, InputStream input) {
    Doc doc = parser.apply(context, input);
    String[] links = linkExtractor.apply(context, doc);

    AssertionResult[] assertionResults = new AssertionResult[assertionFunctions.size()];

    for (int i = 0; i < assertionFunctions.size(); i++) {
      AssertionResult assertionResult = assertionFunctions.get(i).apply(context, doc);
      assertionResults[i] = assertionResult;
    }

    return new Result(links, assertionResults);
  }
}
