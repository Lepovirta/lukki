package io.lepo.lukki.core;

import java.net.URI;
import java.util.Arrays;

public final class CrawlEvent {

  private final URI uri;
  private final Exception error;
  private final AssertionResult[] assertionResults;

  private CrawlEvent(
      final URI uri,
      final Exception error,
      final AssertionResult[] assertionResults
  ) {
    this.uri = uri;
    this.error = error;
    this.assertionResults = assertionResults;
  }

  public static CrawlEvent success(
      final URI uri,
      final AssertionResult[] assertionResults
  ) {
    return new CrawlEvent(uri, null, assertionResults);
  }

  public static CrawlEvent failure(
      final URI uri,
      final Exception error
  ) {
    return new CrawlEvent(uri, error, new AssertionResult[]{});
  }

  public URI getUri() {
    return uri;
  }

  public Exception getError() {
    return error;
  }

  public AssertionResult[] getAssertionResults() {
    return assertionResults;
  }

  public boolean isFailed() {
    return error != null
        || Arrays.stream(assertionResults).anyMatch(r -> r.getErrors().length > 0);
  }

  @Override
  public String toString() {
    return "CrawlEvent{"
        + "uri='" + uri + '\''
        + ", error='" + error + '\''
        + ", assertionResults=" + Arrays.toString(assertionResults)
        + '}';
  }
}
