package io.lepo.lukki.core;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CrawlEngine
    implements
    Closeable,
    BiFunction<CrawlJob, CrawlResult.Bus, CompletableFuture<CrawlJob.Result>> {

  private static final Logger log = LoggerFactory.getLogger(CrawlEngine.class);

  private final AtomicBoolean started;
  private final CrawlClient client;
  private final ScriptRegistry registry;
  private final Filters filters;

  public CrawlEngine(
      final CrawlClient client,
      final ScriptRegistry registry,
      final Filters filters
  ) {
    this.started = new AtomicBoolean(false);
    this.filters = filters;
    this.client = new TrackingCrawlClient(client);
    this.registry = registry;
  }

  @Override
  public CompletableFuture<CrawlJob.Result> apply(
      final CrawlJob job,
      final CrawlResult.Bus crawlResultBus
  ) {
    if (started.compareAndSet(false, true)) {
      log.debug("Starting client");
      client.start();
    }

    final LocalDateTime startTime = LocalDateTime.now();

    return new PerJobCrawlEngine(
        registry, filters, client,
        job, crawlResultBus
    ).get().thenApply(
        endTime -> job.toResult(startTime, endTime)
    );
  }

  @Override
  public void close() throws IOException {
    log.debug("Closing.");
    client.close();
    log.debug("Closed.");
  }
}
