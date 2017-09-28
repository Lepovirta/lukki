package io.lepo.lukki.core;

import java.io.Closeable;
import java.io.InputStream;
import java.util.function.BiConsumer;

public interface CrawlClient
    extends
    Closeable,
    BiConsumer<String, CrawlClient.Callback> {

  interface Callback {

    void onSuccess(String mimeType, InputStream input);

    void onFailure(Exception ex);
  }

  void start();
}
