package io.lepo.lukki.examples;

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
    if (args.length < 1) {
      System.err.println("Please provide a HTTP/HTTPS URI as the first parameter");
      System.exit(1);
    }

    // Read the URI
    final URI uri = URI.create(args[0]);

    // Setting up the crawler
    final Map<String, Script<?>> scripts = new HashMap<>();
    scripts.put(Html.mimeType, Html.script(Collections.emptyList()));
    final ScriptRegistry scriptRegistry = ScriptRegistry.lenient(scripts);
    final CrawlEngine crawler = new CrawlEngine(
        new HttpClient(),
        scriptRegistry,
        Filters.DEFAULT
    );

    // Setting up crawl job
    final CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(uri);
    System.out.println("Crawling URI: " + job.getUri());
    final CrawlEventAggregator aggregator = crawler.apply(job);

    try {
      // Wait for 60 seconds for the crawl to finish and print out the report
      CrawlReport report = aggregator.getReport().get(60, TimeUnit.SECONDS);
      System.out.println(report.statsString());
    } finally {
      crawler.close();
    }
  }
}