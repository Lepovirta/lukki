package io.lepo.lukki.cli;

import io.lepo.lukki.core.CrawlEngine;
import io.lepo.lukki.core.CrawlJob;
import io.lepo.lukki.core.Filters;
import io.lepo.lukki.core.Script;
import io.lepo.lukki.core.ScriptRegistry;
import io.lepo.lukki.html.Html;
import io.lepo.lukki.http.HttpClient;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Main {

  public static void main(String[] args) throws Exception {
    Map<String, Script<?>> scripts = new HashMap<>();
    scripts.put(Html.mimeType, Html.script(Collections.emptyList()));
    ScriptRegistry scriptRegistry = ScriptRegistry.lenient(scripts);
    CrawlEngine crawler = new CrawlEngine(
        new HttpClient(),
        scriptRegistry,
        new Filters(Filters.LinkFilter.skipNone, Filters.DocumentFilter.skipForeignHost)
    );

    CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(URI.create(args[0]));

    LocalDateTime startTime = LocalDateTime.now();

    System.out.println("Crawling URI: " + job.getUri());
    CompletableFuture<LocalDateTime> crawlFuture = crawler.apply(
        job,
        result -> System.out.println(result.toString())
    );

    try {
      LocalDateTime endTime = crawlFuture.get(60, TimeUnit.SECONDS);
      Duration crawlDuration = Duration.between(startTime, endTime);
      System.out.println("Crawl duration in seconds: " + crawlDuration.getSeconds());
    } finally {
      crawler.close();
    }
  }
}
