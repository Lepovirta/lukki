package io.lepo.lukki.core;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Consumer;

public final class CrawlResult {

  private final URI uri;
  private final Exception error;
  private final AssertionResult[] assertionResults;

  private CrawlResult(
      URI uri,
      Exception error,
      AssertionResult[] assertionResults
  ) {
    this.uri = uri;
    this.error = error;
    this.assertionResults = assertionResults;
  }

  public static CrawlResult success(
      URI uri,
      AssertionResult[] assertionResults
  ) {
    return new CrawlResult(uri, null, assertionResults);
  }

  public static CrawlResult failure(
      URI uri,
      Exception error
  ) {
    return new CrawlResult(uri, error, new AssertionResult[]{});
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

  @Override
  public String toString() {
    return "CrawlResult{"
        + "uri='" + uri + '\''
        + ", error='" + error + '\''
        + ", assertionResults=" + Arrays.toString(assertionResults)
        + '}';
  }

  public interface Bus extends Consumer<CrawlResult> {

  }
}
