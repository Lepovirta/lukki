package io.lepo.lukki.core;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PerJobCrawlEngine implements Runnable {

  private final Logger log = LoggerFactory.getLogger(PerJobCrawlEngine.class);

  private final ConcurrentMap<String, Boolean> visitedUrls;
  private final ScriptRegistry registry;
  private final Filters filters;
  private final CrawlJob job;
  private final CrawlObserver observer;
  private final TrackingCrawlClient client;
  private LocalDateTime startTime = null;

  PerJobCrawlEngine(
      final ScriptRegistry registry,
      final Filters filters,
      final CrawlClient client,
      final CrawlJob job,
      final CrawlObserver observer
  ) {
    this.registry = registry;
    this.filters = filters;
    this.job = job;
    this.observer = observer;
    this.visitedUrls = new ConcurrentHashMap<>(100);
    this.client = new TrackingCrawlClient(client, this::onComplete);
  }

  private void enqueueUrl(final URI uri) {
    visitedUrls.computeIfAbsent(
        uri.toString(),
        k -> {
          log.debug("Fetching URL: {}", uri);
          fetchUrl(uri);
          return true;
        }
    );
  }

  private void fetchUrl(final URI uri) {
    client.accept(
        uri,
        new HandleFetch(uri)
    );
  }

  @Override
  public void run() {
    startTime = LocalDateTime.now();
    enqueueUrl(job.getUri());
  }

  private void onComplete(LocalDateTime endTime) {
    observer.onComplete(job.toResult(startTime, endTime));
  }

  private class HandleFetch implements CrawlClient.Callback {

    private final URI uri;

    HandleFetch(URI uri) {
      this.uri = uri;
    }

    @Override
    public void onSuccess(final String mimeType, final Charset charset, final InputStream input) {
      CrawlContext context = new CrawlContext(job, uri, mimeType, charset);

      if (!filters.shouldProcessDocument(context)) {
        log.debug("Skipping handling for document at URI [{}]", uri);
        observer.onNext(CrawlEvent.success(uri, new AssertionResult[0]));
        return;
      }

      try {
        log.debug("Executing handler for mime type [{}] and URI [{}]", mimeType, uri);
        Script.Result scriptResult = registry.run(context, input);

        URI[] links = scriptResult.getLinks();
        log.debug("Found {} URIs from URI [{}]", links.length, uri);
        for (URI link : links) {
          if (filters.shouldProcessLink(context, link)) {
            enqueueUrl(link);
          } else {
            log.debug("Skipping fetching of URI [{}]", uri);
          }
        }

        observer.onNext(CrawlEvent.success(
            uri,
            scriptResult.getAssertionResults()
        ));
      } catch (Exception ex) {
        log.debug("Execution of successful fetch failed!", ex);
        handleFailure(ex);
      }
    }

    @Override
    public void onFailure(final Exception ex) {
      log.debug("Fetch failed for URI [{}]", ex);
      handleFailure(ex);
    }

    private void handleFailure(final Exception ex) {
      observer.onNext(CrawlEvent.failure(
          uri,
          ex
      ));
    }
  }
}
