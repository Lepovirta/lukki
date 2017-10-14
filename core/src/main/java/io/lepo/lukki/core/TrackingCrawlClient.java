package io.lepo.lukki.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TrackingCrawlClient implements CrawlClient {

  private static final Logger log = LoggerFactory.getLogger(TrackingCrawlClient.class);

  private final Phaser phaser;
  private final CrawlClient client;
  private final AfterFetch afterFetch;
  private final Consumer<LocalDateTime> onComplete;

  TrackingCrawlClient(CrawlClient client, Consumer<LocalDateTime> onComplete) {
    this.client = client;
    this.onComplete = onComplete;
    this.phaser = new Phaser();
    this.afterFetch = new AfterFetch();
  }

  @Override
  public void start() {
    client.start();
  }

  @Override
  public void close() throws IOException {
    client.close();
    phaser.forceTermination();
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
        log.debug("Phaser has been cancelled.");
        return;
      }

      if (phaser.getUnarrivedParties() == 0) {
        log.debug("No parties left in phaser. Completing.");
        onComplete.accept(LocalDateTime.now());
      }
    }
  }
}
