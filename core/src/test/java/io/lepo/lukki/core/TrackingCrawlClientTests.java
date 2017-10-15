package io.lepo.lukki.core;

import io.lepo.lukki.core.ReadResponses.Response;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrackingCrawlClientTests {

  private final static int[] iterations = {1, 11, 47};

  @Test
  @DisplayName("Tracker should start and close the underlying client")
  void testStartAndClose() throws Exception {
    final DummyClient dummyClient = new DummyClient();
    final TrackingCrawlClient trackingClient = new TrackingCrawlClient(
        dummyClient,
        (time) -> {
        }
    );

    trackingClient.start();
    Assertions.assertThat(dummyClient.isStarted()).isTrue();

    trackingClient.close();
    Assertions.assertThat(dummyClient.isClosed()).isTrue();
  }

  @Test
  @DisplayName("Tracker should finish after every task")
  void testFinishAfterAll() throws Exception {
    for (int iter : iterations) {
      final String rootUri = "http://localhost/";
      final AtomicInteger remainingIterations = new AtomicInteger(iter);
      final DummyClient dummyClient = new DummyClient();
      final CompletableFuture<LocalDateTime> completed = new CompletableFuture<>();
      final TrackingCrawlClient trackingClient = new TrackingCrawlClient(
          dummyClient,
          completed::complete
      );
      final ReadResponses callback = new ReadResponses(trackingClient, () -> {
        final int i = remainingIterations.decrementAndGet();
        if (i > 0) {
          return URI.create(rootUri + "/" + i);
        }
        return null;
      });

      trackingClient.accept(URI.create(rootUri), callback);
      final LocalDateTime dt = completed.get(2, TimeUnit.SECONDS);
      final List<Response> responses = callback.getResponses();

      Assertions
          .assertThat(responses)
          .allMatch(
              r -> r.getTimestamp().isEqual(dt) || r.getTimestamp().isBefore(dt)
          );

      Assertions
          .assertThat(responses)
          .hasSize(iter);
    }
  }

}
