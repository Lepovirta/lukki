package io.lepo.lukki.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CrawlEventAggregator implements CrawlObserver {

  private final CompletableFuture<CrawlReport> report;
  private final ConcurrentLinkedQueue<CrawlEvent> crawlEvents;

  public CrawlEventAggregator() {
    this.report = new CompletableFuture<>();
    this.crawlEvents = new ConcurrentLinkedQueue<>();
  }

  @Override
  public void onComplete(CrawlJob.Result result) {
    final CrawlEvent[] eventsArray = new CrawlEvent[crawlEvents.size()];
    crawlEvents.toArray(eventsArray);
    report.complete(CrawlReport.create(result, eventsArray));
  }

  @Override
  public void onNext(CrawlEvent event) {
    crawlEvents.offer(event);
  }

  public CompletableFuture<CrawlReport> getReport() {
    return report;
  }
}
