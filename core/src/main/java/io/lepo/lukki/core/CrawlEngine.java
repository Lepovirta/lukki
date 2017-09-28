package io.lepo.lukki.core;

import io.lepo.lukki.core.CrawlClient.Callback;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CrawlEngine implements Consumer<String> {

  private static final Logger log = LoggerFactory.getLogger(CrawlEngine.class);

  private final ForkJoinPool pool;
  private final Phaser phaser;
  private final ConcurrentMap<String, Boolean> visitedUrls;
  private final CrawlClient client;
  private final ScriptRegistry registry;

  public CrawlEngine(
      CrawlClient client,
      ScriptRegistry registry
  ) {
    this.pool = new ForkJoinPool(2);
    this.phaser = new Phaser(1);
    this.visitedUrls = new ConcurrentHashMap<>();
    this.client = client;
    this.registry = registry;
  }

  public void run(String originUrl) {
    log.debug("Starting");
    client.start();

    enqueueUrl(originUrl, originUrl);
    phaser.arriveAndAwaitAdvance();

    log.debug("Finished. Closing.");

    try {
      client.close();
    } catch (IOException ex) {
      log.error("Error occurred while closing the client", ex);
    }

    log.debug("Closed.");
  }

  private void enqueueUrl(final String originUrl, final String url) {
    visitedUrls.computeIfAbsent(
        url,
        k -> {
          log.debug("Enqueuing fetch for URL: {}", url);
          phaser.register();
          pool.execute(() -> fetchUrl(originUrl, url));
          return true;
        }
    );
  }

  private void fetchUrl(final String originUrl, final String url) {
    client.accept(url, new Callback() {
      @Override
      public void onSuccess(String mimeType, InputStream input) {
        try {
          CrawlContext context = new CrawlContext(originUrl, url, mimeType);

          log.debug("Executing handler for mime type [{}] and URL [{}]", mimeType, url);
          ScriptRegistry.Result scriptResult = registry.run(context, input);

          String[] links = scriptResult.getLinks();
          log.debug("Found {} URLs from URL [{}]", links.length, url);
          for (String link : links) {
            enqueueUrl(originUrl, link);
          }

          CrawlResult crawlResult = CrawlResult.success(
              url,
              scriptResult.getAssertionResults()
          );

          System.out.println(crawlResult); // TODO
        } catch (Exception e) {
          log.debug("Execution of successful fetch failed!", e);
          handleFailure(e);
        } finally {
          phaser.arriveAndDeregister();
        }
      }

      @Override
      public void onFailure(Exception ex) {
        try {
          log.debug("Fetch failed for URL [{}]", ex);
          handleFailure(ex);
        } finally {
          phaser.arriveAndDeregister();
        }
      }

      private void handleFailure(Exception ex) {
        CrawlResult crawlResult = CrawlResult.failure(
            url,
            ex.getMessage()
        );
        System.out.println(crawlResult); // TODO
      }
    });
  }

  @Override
  public void accept(String originUrl) {
    run(originUrl);
  }
}
