package io.lepo.lukki.core;

import io.lepo.lukki.core.CrawlResult.Bus;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PerJobCrawlEngine
    implements Supplier<CompletableFuture<LocalDateTime>> {

  private final Logger log = LoggerFactory.getLogger(PerJobCrawlEngine.class);

  private final ConcurrentMap<String, Boolean> visitedUrls;
  private final ScriptRegistry registry;
  private final Filters filters;
  private final CrawlJob job;
  private final TrackingCrawlClient client;
  private final CrawlResult.Bus crawlResultBus;

  PerJobCrawlEngine(
      ScriptRegistry registry,
      Filters filters,
      CrawlClient client,
      CrawlJob job,
      Bus crawlResultBus
  ) {
    this.registry = registry;
    this.filters = filters;
    this.job = job;
    this.visitedUrls = new ConcurrentHashMap<>();
    this.client = new TrackingCrawlClient(client);
    this.crawlResultBus = crawlResultBus;
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
  public CompletableFuture<LocalDateTime> get() {
    enqueueUrl(job.getUri());
    return client.getFuture();
  }

  private class HandleFetch implements CrawlClient.Callback {

    private final URI uri;

    HandleFetch(URI uri) {
      this.uri = uri;
    }

    @Override
    public void onSuccess(String mimeType, Charset charset, InputStream input) {
      CrawlContext context = new CrawlContext(job, uri, mimeType, charset);

      if (!filters.shouldProcessDocument(context)) {
        log.debug("Skipping handling for document at URI [{}]", uri);
        crawlResultBus.accept(CrawlResult.success(uri, new AssertionResult[0]));
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

        CrawlResult crawlResult = CrawlResult.success(
            uri,
            scriptResult.getAssertionResults()
        );

        crawlResultBus.accept(crawlResult);
      } catch (Exception ex) {
        log.debug("Execution of successful fetch failed!", ex);
        handleFailure(ex);
      }
    }

    @Override
    public void onFailure(Exception ex) {
      log.debug("Fetch failed for URI [{}]", ex);
      handleFailure(ex);
    }

    private void handleFailure(Exception ex) {
      CrawlResult crawlResult = CrawlResult.failure(
          uri,
          ex
      );
      crawlResultBus.accept(crawlResult);
    }
  }
}
