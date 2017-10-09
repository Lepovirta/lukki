package io.lepo.lukki.core;

import java.net.URI;
import java.nio.charset.Charset;

public final class CrawlContext {

  private final CrawlJob job;
  private final URI uri;
  private final String mimeType;
  private final Charset charset;

  public CrawlContext(
      CrawlJob job,
      URI uri,
      String mimeType,
      Charset charset
  ) {
    this.job = job;
    this.uri = uri;
    this.mimeType = mimeType;
    this.charset = charset;
  }

  public CrawlJob getJob() {
    return job;
  }

  public URI getUri() {
    return uri;
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
        + "job=" + job.toString()
        + ", uri='" + uri + '\''
        + ", mimeType='" + mimeType + '\''
        + '}';
  }
}
