package io.lepo.lukki.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TrackingCrawlClient implements CrawlClient {

  private static final Logger log = LoggerFactory.getLogger(TrackingCrawlClient.class);

  private final Phaser phaser;
  private final CrawlClient client;
  private final AfterFetch afterFetch;
  private final CompletableFuture<LocalDateTime> completion;

  TrackingCrawlClient(CrawlClient client) {
    this.client = client;
    this.phaser = new Phaser();
    this.afterFetch = new AfterFetch();
    this.completion = new CompletableFuture<>();
  }

  @Override
  public void start() {
    client.start();
  }

  @Override
  public void close() throws IOException {
    client.close();
    phaser.forceTermination();
    completion.cancel(true);
  }

  @Override
  public void accept(URI uri, CrawlClient.Callback callback) {
    phaser.register();
    CrawlClient.Callback[] callbacks = {
        callback,
        afterFetch
    };
    client.accept(uri, CrawlClient.Callback.sequence(callbacks));
  }

  public CompletableFuture<LocalDateTime> getFuture() {
    return completion;
  }

  private final class AfterFetch implements CrawlClient.Callback {

    @Override
    public void onSuccess(String mimeType, Charset charset, InputStream input) {
      afterFetch();
    }

    @Override
    public void onFailure(Exception ex) {
      afterFetch();
    }

    private void afterFetch() {
      int phaseNumber = phaser.arriveAndDeregister();
      if (phaseNumber < 0) {
        log.debug("Phaser has been cancelled. Cancelling.");
        completion.cancel(true);
        return;
      }

      if (phaser.getUnarrivedParties() == 0) {
        log.debug("No parties left in phaser. Completing.");
        completion.complete(LocalDateTime.now());
      }
    }
  }
}
