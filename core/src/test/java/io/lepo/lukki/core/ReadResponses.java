package io.lepo.lukki.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;

final class ReadResponses implements CrawlClient.Callback {

  private final ConcurrentLinkedQueue<Response> responses = new ConcurrentLinkedQueue<>();
  private final Supplier<URI> getNext;
  private final CrawlClient client;

  ReadResponses(CrawlClient client, Supplier<URI> getNext) {
    this.client = client;
    this.getNext = getNext;
  }

  private static String inputStreamToString(InputStream input) throws IOException {
    final StringBuilder sb = new StringBuilder();
    try (
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(input, Charset.defaultCharset())
        )
    ) {
      int c = 0;
      while ((c = reader.read()) != -1) {
        sb.append((char) c);
      }
    }
    return sb.toString();
  }

  List<Response> getResponses() {
    return new ArrayList<>(responses);
  }

  @Override
  public void onSuccess(String mimeType, Charset charset, InputStream input) {
    try {
      final String content = inputStreamToString(input);
      responses.offer(new Response(LocalDateTime.now(), content));
      final URI next = getNext.get();
      if (next != null) {
        client.accept(next, this);
      }
    } catch (Exception ex) {
      Assertions.fail("Error during getNext", ex);
    }
  }

  @Override
  public void onFailure(Exception ex) {
    Assertions.fail("Error during onFailure", ex);
  }

  static final class Response {

    private final LocalDateTime timestamp;
    private final String contents;

    public Response(LocalDateTime timestamp, String contents) {
      this.timestamp = timestamp;
      this.contents = contents;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }

    public String getContents() {
      return contents;
    }

    @Override
    public String toString() {
      return "Response{"
          + "timestamp=" + timestamp
          + ", contents='" + contents + '\''
          + '}';
    }
  }
}
