package io.lepo.lukki.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class Script<Entity> {

  public static final Script<Object> throwOnEverything = new Script<>(
      (context, input) -> {
        throw new UnsupportedOperationException("No script found for mime type: " + context.getMimeType());
      },
      (context, entity) -> new String[0],
      new ArrayList<>()
  );

  public static final Script<Object> acceptEverything = new Script<>(
      (context, input) -> new Object(),
      (context, entity) -> new String[0],
      new ArrayList<>()
  );

  private final BiFunction<CrawlContext, InputStream, Entity> mapper;
  private final BiFunction<CrawlContext, Entity, String[]> linkExtractor;
  private final List<BiFunction<CrawlContext, Entity, AssertionResult>> assertionFunctions;

  public Script(
      BiFunction<CrawlContext, InputStream, Entity> mapper,
      BiFunction<CrawlContext, Entity, String[]> linkExtractor,
      List<BiFunction<CrawlContext, Entity, AssertionResult>> assertionFunctions
  ) {
    this.mapper = mapper;
    this.linkExtractor = linkExtractor;
    this.assertionFunctions = assertionFunctions;
  }

  public ScriptRegistry.Result run(CrawlContext context, InputStream input) {
    Entity entity = mapper.apply(context, input);
    String[] links = linkExtractor.apply(context, entity);

    AssertionResult[] assertionResults = new AssertionResult[assertionFunctions.size()];

    for (int i = 0; i < assertionFunctions.size(); i++) {
      AssertionResult assertionResult = assertionFunctions.get(i).apply(context, entity);
      assertionResults[i] = assertionResult;
    }

    return new ScriptRegistry.Result(links, assertionResults);
  }
}
