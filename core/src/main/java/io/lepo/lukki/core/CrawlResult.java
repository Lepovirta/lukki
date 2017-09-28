package io.lepo.lukki.core;


import java.util.Arrays;

public final class CrawlResult {

  private final String url;
  private final boolean fetchOk;
  private final String summary;
  private final AssertionResult[] assertionResults;

  private CrawlResult(
      String url,
      boolean fetchOk,
      String summary,
      AssertionResult[] assertionResults
  ) {
    this.url = url;
    this.fetchOk = fetchOk;
    this.summary = summary;
    this.assertionResults = assertionResults;
  }

  public String getUrl() {
    return url;
  }

  public boolean isFetchOk() {
    return fetchOk;
  }

  public AssertionResult[] getAssertionResults() {
    return assertionResults;
  }

  @Override
  public String toString() {
    return "CrawlResult{" +
        "url='" + url + '\'' +
        ", fetchOk=" + fetchOk +
        ", summary='" + summary + '\'' +
        ", assertionResults=" + Arrays.toString(assertionResults) +
        '}';
  }

  public static CrawlResult success(
      String url,
      AssertionResult[] assertionResults
  ) {
    return new CrawlResult(url, true, "", assertionResults);
  }

  public static CrawlResult failure(
      String url,
      String summary
  ) {
    return new CrawlResult(url, false, summary, new AssertionResult[]{});
  }
}
