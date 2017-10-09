package io.lepo.lukki.core;

import io.lepo.lukki.core.CrawlClient.Callback;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CrawlEngine implements Consumer<CrawlJob> {

  private static final Logger log = LoggerFactory.getLogger(CrawlEngine.class);

  private final ForkJoinPool pool;
  private final Phaser phaser;
  private final ConcurrentMap<String, Boolean> visitedUrls;
  private final CrawlClient client;
  private final ScriptRegistry registry;
  private final Filters filters;

  public CrawlEngine(
      CrawlClient client,
      ScriptRegistry registry,
      Filters filters
  ) {
    this.filters = filters;
    this.pool = new ForkJoinPool(2);
    this.phaser = new Phaser(1);
    this.visitedUrls = new ConcurrentHashMap<>();
    this.client = client;
    this.registry = registry;
  }

  public void run(CrawlJob job) {
    log.debug("Starting");
    client.start();

    enqueueUrl(job, job.getUrl());
    phaser.arriveAndAwaitAdvance();

    log.debug("Finished. Closing.");

    try {
      client.close();
    } catch (IOException ex) {
      log.error("Error occurred while closing the client", ex);
    }

    log.debug("Closed.");
  }

  private void enqueueUrl(final CrawlJob job, final String url) {
    visitedUrls.computeIfAbsent(
        url,
        k -> {
          log.debug("Enqueuing fetch for URL: {}", url);
          phaser.register();
          pool.execute(() -> fetchUrl(job, url));
          return true;
        }
    );
  }

  private void fetchUrl(final CrawlJob job, final String url) {
    Callback[] callbacks = {
        new HandleFetch(job, url),
        new AfterFetch()
    };
    client.accept(
        url,
        CrawlClient.Callback.sequence(callbacks)
    );
  }

  @Override
  public void accept(CrawlJob job) {
    run(job);
  }

  private class HandleFetch implements CrawlClient.Callback {

    private final CrawlJob job;
    private final String url;

    HandleFetch(CrawlJob job, String url) {
      this.job = job;
      this.url = url;
    }

    @Override
    public void onSuccess(String mimeType, Charset charset, InputStream input) {
      CrawlContext context = new CrawlContext(job, url, mimeType, charset);

      if (!filters.shouldProcessDocument(context)) {
        log.debug("Skipping handling for document at URL [{}]", url);
        return; // TODO
      }

      try {
        log.debug("Executing handler for mime type [{}] and URL [{}]", mimeType, url);
        Script.Result scriptResult = registry.run(context, input);

        String[] links = scriptResult.getLinks();
        log.debug("Found {} URLs from URL [{}]", links.length, url);
        for (String link : links) {
          if (filters.shouldProcessLink(context, link)) {
            enqueueUrl(job, link);
          } else {
            log.debug("Skipping fetching of URL [{}]", url);
          }
        }

        CrawlResult crawlResult = CrawlResult.success(
            url,
            scriptResult.getAssertionResults()
        );

        System.out.println(crawlResult); // TODO
      } catch (Exception ex) {
        log.debug("Execution of successful fetch failed!", ex);
        handleFailure(ex);
      }
    }

    @Override
    public void onFailure(Exception ex) {
      log.debug("Fetch failed for URL [{}]", ex);
      handleFailure(ex);
    }

    private void handleFailure(Exception ex) {
      CrawlResult crawlResult = CrawlResult.failure(
          url,
          ex.getMessage()
      );
      System.out.println(crawlResult); // TODO
    }
  }

  private class AfterFetch implements Callback {

    @Override
    public void onSuccess(String mimeType, Charset charset, InputStream input) {
      phaser.arriveAndDeregister();
    }

    @Override
    public void onFailure(Exception ex) {
      phaser.arriveAndDeregister();
    }
  }
}
