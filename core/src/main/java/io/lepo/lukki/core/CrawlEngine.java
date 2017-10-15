package io.lepo.lukki.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CrawlEngine
    implements
    Closeable,
    BiConsumer<CrawlJob, CrawlObserver>,
    Function<CrawlJob, CrawlEventAggregator> {

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
    this.client = client;
    this.registry = registry;
  }

  @Override
  public void accept(
      final CrawlJob job,
      final CrawlObserver observer
  ) {
    if (started.compareAndSet(false, true)) {
      log.debug("Starting client");
      client.start();
    }

    new PerJobCrawlEngine(
        registry, filters, client,
        job, observer
    ).run();
  }

  @Override
  public CrawlEventAggregator apply(CrawlJob crawlJob) {
    final CrawlEventAggregator aggregator = new CrawlEventAggregator();
    this.accept(crawlJob, aggregator);
    return aggregator;
  }

  @Override
  public void close() throws IOException {
    log.debug("Closing.");
    client.close();
    log.debug("Closed.");
  }
}
