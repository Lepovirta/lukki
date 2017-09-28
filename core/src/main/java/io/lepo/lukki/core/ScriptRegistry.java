package io.lepo.lukki.core;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class ScriptRegistry {

  public static class Result {

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

  private final Map<String, Script<?>> scripts;
  private final Script<?> defaultScript;

  public ScriptRegistry(Script<?> defaultScript, Map<String, Script<?>> scripts) {
    this.scripts = new HashMap<>(scripts);
    this.defaultScript = defaultScript;
  }

  public Result run(CrawlContext context, InputStream input) {
    Script<?> script = scripts.getOrDefault(context.getMimeType(), defaultScript);
    return script.run(context, input);
  }

  public static ScriptRegistry strict(Map<String, Script<?>> scripts) {
    return new ScriptRegistry(Script.throwOnEverything, scripts);
  }

  public static ScriptRegistry lenient(Map<String, Script<?>> scripts) {
    return new ScriptRegistry(Script.acceptEverything, scripts);
  }

  public static ScriptRegistry custom(Script<?> defaultScript, Map<String, Script<?>> scripts) {
    return new ScriptRegistry(defaultScript, scripts);
  }
}
