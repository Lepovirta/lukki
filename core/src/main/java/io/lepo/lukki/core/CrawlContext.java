package io.lepo.lukki.core;

public final class CrawlContext {

  private final String originUrl;
  private final String url;
  private final String mimeType;

  public CrawlContext(
      String originUrl,
      String url,
      String mimeType
  ) {
    this.originUrl = originUrl;
    this.url = url;
    this.mimeType = mimeType;
  }

  public String getOriginUrl() {
    return originUrl;
  }

  public String getUrl() {
    return url;
  }

  public String getMimeType() {
    return mimeType;
  }

  @Override
  public String toString() {
    return "CrawlContext{" +
        "originUrl='" + originUrl + '\'' +
        ", url='" + url + '\'' +
        ", mimeType='" + mimeType + '\'' +
        '}';
  }
}
