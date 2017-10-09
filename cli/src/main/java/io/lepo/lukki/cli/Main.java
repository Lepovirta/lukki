package io.lepo.lukki.cli;

import io.lepo.lukki.core.CrawlEngine;
import io.lepo.lukki.core.CrawlJob;
import io.lepo.lukki.core.Filters;
import io.lepo.lukki.core.Script;
import io.lepo.lukki.core.ScriptRegistry;
import io.lepo.lukki.html.Html;
import io.lepo.lukki.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

  public static void main(String[] args) throws Exception {
    Map<String, Script<?>> scripts = new HashMap<>();
    scripts.put(Html.mimeType, Html.script(new ArrayList<>()));
    ScriptRegistry scriptRegistry = ScriptRegistry.lenient(scripts);

    CrawlEngine crawler = new CrawlEngine(
        new HttpClient(),
        scriptRegistry,
        new Filters(Filters.LinkFilter.skipNone, Filters.DocumentFilter.skipForeignHost)
    );
    crawler.run(CrawlJob.withHostsStartingWithUrlHost(args[0]));
  }
}
