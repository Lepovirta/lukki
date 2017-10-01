package io.lepo.lukki.core;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;

public interface CrawlClient
    extends
    Closeable,
    BiConsumer<String, CrawlClient.Callback> {

  interface Callback {

    void onSuccess(String mimeType, Charset charset, InputStream input);

    void onFailure(Exception ex);

    default void empty() {
      onFailure(new RuntimeException("Empty response"));
    }

    static Callback sequence(Callback[] callbacks) {
      return new Sequence(callbacks);
    }

    class Sequence implements Callback {

      private final Callback[] callbacks;

      public Sequence(Callback[] callbacks) {
        this.callbacks = callbacks;
      }

      @Override
      public void onSuccess(String mimeType, Charset charset, InputStream input) {
        for (Callback callback : callbacks) {
          callback.onSuccess(mimeType, charset, input);
        }
      }

      @Override
      public void onFailure(Exception ex) {
        for (Callback callback : callbacks) {
          callback.onFailure(ex);
        }
      }
    }
  }

  void start();
}
