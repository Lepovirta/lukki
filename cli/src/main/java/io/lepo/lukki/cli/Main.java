package io.lepo.lukki.cli;

import io.lepo.lukki.core.CrawlEngine;
import io.lepo.lukki.core.CrawlEventAggregator;
import io.lepo.lukki.core.CrawlJob;
import io.lepo.lukki.core.CrawlReport;
import io.lepo.lukki.core.Filters;
import io.lepo.lukki.core.Script;
import io.lepo.lukki.core.ScriptRegistry;
import io.lepo.lukki.html.Html;
import io.lepo.lukki.http.HttpClient;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Main {

  public static void main(String[] args) throws Exception {
    final Map<String, Script<?>> scripts = new HashMap<>();
    scripts.put(Html.mimeType, Html.script(Collections.emptyList()));
    final ScriptRegistry scriptRegistry = ScriptRegistry.lenient(scripts);
    final CrawlEngine crawler = new CrawlEngine(
        new HttpClient(),
        scriptRegistry,
        new Filters(Filters.LinkFilter.skipNone, Filters.DocumentFilter.skipForeignHost)
    );

    final CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(URI.create(args[0]));
    System.out.println("Crawling URI: " + job.getUri());
    final CrawlEventAggregator aggregator = crawler.apply(job);

    try {
      CrawlReport report = aggregator.getReport().get(60, TimeUnit.SECONDS);
      System.out.println(report.statsString());
    } finally {
      crawler.close();
    }
  }
}
