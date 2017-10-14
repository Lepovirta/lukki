package io.lepo.lukki.core;

import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;

public interface CrawlClient
    extends
    Closeable,
    BiConsumer<URI, CrawlClient.Callback> {

  interface Callback {

    void onSuccess(String mimeType, Charset charset, InputStream input);

    void onFailure(Exception ex);

    default void empty() {
      onFailure(new RuntimeException("Empty response"));
    }
  }

  void start();
}
