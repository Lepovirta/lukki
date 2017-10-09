package io.lepo.lukki.core;

import java.net.URI;
import java.util.Arrays;

public final class CrawlResult {

  private final URI uri;
  private final boolean fetchOk;
  private final String summary;
  private final AssertionResult[] assertionResults;

  private CrawlResult(
      URI uri,
      boolean fetchOk,
      String summary,
      AssertionResult[] assertionResults
  ) {
    this.uri = uri;
    this.fetchOk = fetchOk;
    this.summary = summary;
    this.assertionResults = assertionResults;
  }

  public static CrawlResult success(
      URI uri,
      AssertionResult[] assertionResults
  ) {
    return new CrawlResult(uri, true, "", assertionResults);
  }

  public static CrawlResult failure(
      URI uri,
      String summary
  ) {
    return new CrawlResult(uri, false, summary, new AssertionResult[]{});
  }

  public URI getUri() {
    return uri;
  }

  public boolean isFetchOk() {
    return fetchOk;
  }

  public AssertionResult[] getAssertionResults() {
    return assertionResults;
  }

  @Override
  public String toString() {
    return "CrawlResult{"
        + "uri='" + uri + '\''
        + ", fetchOk=" + fetchOk
        + ", summary='" + summary + '\''
        + ", assertionResults=" + Arrays.toString(assertionResults)
        + '}';
  }
}
