package io.lepo.lukki.core;

import java.nio.charset.Charset;

public final class CrawlContext {

  private final CrawlJob job;
  private final String url;
  private final String mimeType;
  private final Charset charset;

  public CrawlContext(
      CrawlJob job,
      String url,
      String mimeType,
      Charset charset
  ) {
    this.job = job;
    this.url = url;
    this.mimeType = mimeType;
    this.charset = charset;
  }

  public CrawlJob getJob() {
    return job;
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
        + "job='" + job.toString() + '\''
        + ", url='" + url + '\''
        + ", mimeType='" + mimeType + '\''
        + '}';
  }
}
