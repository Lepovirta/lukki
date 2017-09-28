package io.lepo.lukki.cli;

import io.lepo.lukki.core.CrawlEngine;
import io.lepo.lukki.core.Script;
import io.lepo.lukki.core.ScriptRegistry;
import io.lepo.lukki.html.Html;
import io.lepo.lukki.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

  public static void main(String[] args) {
    Map<String, Script<?>> scripts = new HashMap<>();
    scripts.put(Html.mimeType, Html.script(new ArrayList<>()));
    ScriptRegistry scriptRegistry = ScriptRegistry.lenient(scripts);

    CrawlEngine crawler = new CrawlEngine(new HttpClient(), scriptRegistry);
    crawler.run("https://lepo.io/");
  }
}