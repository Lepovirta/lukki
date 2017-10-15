package io.lepo.lukki.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

final class DummyClient implements CrawlClient {

  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean closed = new AtomicBoolean(false);

  @Override
  public void start() {
    started.compareAndSet(false, true);
  }

  @Override
  public void close() throws IOException {
    closed.compareAndSet(false, true);
  }

  boolean isStarted() {
    return started.get();
  }

  boolean isClosed() {
    return closed.get();
  }

  @Override
  public void accept(URI uri, Callback callback) {
    final Charset charset = Charset.defaultCharset();
    callback.onSuccess(
        "dummy",
        charset,
        new ByteArrayInputStream(uri.toString().getBytes(charset))
    );
  }
}
