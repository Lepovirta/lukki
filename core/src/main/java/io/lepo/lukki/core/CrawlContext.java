package io.lepo.lukki.core;

import java.nio.charset.Charset;

public final class CrawlContext {

  private final String originUrl;
  private final String url;
  private final String mimeType;
  private final Charset charset;

  public CrawlContext(
      String originUrl,
      String url,
      String mimeType,
      Charset charset
  ) {
    this.originUrl = originUrl;
    this.url = url;
    this.mimeType = mimeType;
    this.charset = charset;
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

  public Charset getCharset() {
    return charset;
  }

  @Override
  public String toString() {
    return "CrawlContext{"
        + "originUrl='" + originUrl + '\''
        + ", url='" + url + '\''
        + ", mimeType='" + mimeType + '\''
        + '}';
  }
}
