package io.lepo.lukki.core;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class ScriptRegistry {

  private final Map<String, Script<?>> scripts;
  private final Script<?> defaultScript;

  public ScriptRegistry(Script<?> defaultScript, Map<String, Script<?>> scripts) {
    this.scripts = new HashMap<>(scripts);
    this.defaultScript = defaultScript;
  }

  public Script.Result run(CrawlContext context, InputStream input) {
    Script<?> script = scripts.getOrDefault(context.getMimeType(), defaultScript);
    return script.run(context, input);
  }

  public static ScriptRegistry strict(Map<String, Script<?>> scripts) {
    return new ScriptRegistry(Script.rejectAll, scripts);
  }

  public static ScriptRegistry lenient(Map<String, Script<?>> scripts) {
    return new ScriptRegistry(Script.acceptAll, scripts);
  }

  public static ScriptRegistry custom(Script<?> defaultScript, Map<String, Script<?>> scripts) {
    return new ScriptRegistry(defaultScript, scripts);
  }
}
