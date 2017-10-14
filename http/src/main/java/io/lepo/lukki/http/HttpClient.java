package io.lepo.lukki.http;

import io.lepo.lukki.core.CrawlClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpClient implements CrawlClient {

  private static final Logger log = LoggerFactory.getLogger(CrawlClient.class);

  private final CloseableHttpAsyncClient client;

  public HttpClient() {
    this(HttpAsyncClients.createDefault());
  }

  public HttpClient(CloseableHttpAsyncClient client) {
    this.client = client;
  }

  @Override
  public void close() throws IOException {
    log.debug("Closing client");
    client.close();
  }

  @Override
  public void accept(URI uri, Callback callback) {
    try {
      log.debug("Building HTTP get for URI [{}].", uri);
      HttpGet get = new HttpGet(uri);
      client.execute(get, new FutureCallback<HttpResponse>() {
        @Override
        public void completed(HttpResponse result) {
          try {
            HttpEntity entity = result.getEntity();
            if (entity == null) {
              log.debug("No entity available for URI [{}]", uri);
              callback.empty();
              return;
            }

            ContentType contentType = ContentType.getOrDefault(entity);
            String mimeType = contentType.getMimeType();
            Charset charset = contentType.getCharset();
            if (charset == null) {
              charset = Charset.defaultCharset();
            }

            try (InputStream input = entity.getContent()) {
              log.debug("Got content with content type [{}]", contentType);
              callback.onSuccess(mimeType, charset, input);
            } catch (IOException ex) {
              log.debug("Failed to read content for URI [{}]. Reason: {}", uri, ex.getMessage());
              callback.onFailure(ex);
            }
          } catch (Exception ex) {
            log.debug(
                "Failure happened while handling the result for URI [{}]: Reason: {}",
                uri,
                ex.getMessage()
            );
            callback.onFailure(ex);
          }
        }

        @Override
        public void failed(Exception ex) {
          log.debug("Failed HTTP GET for URI [{}]. Reason: {}", uri, ex.getMessage());
          callback.onFailure(ex);
        }

        @Override
        public void cancelled() {
          log.debug("Cancelled HTTP GET for URI [{}]", uri);
          callback.onFailure(new RuntimeException("Cancelled HTTP GET"));
        }
      });
    } catch (IllegalArgumentException ex) {
      log.debug(
          "Caught failure before executing HTTP client request."
              + "Most likely caused by invalid URI."
      );
      callback.onFailure(ex);
    }
  }

  @Override
  public void start() {
    log.debug("Starting client");
    client.start();
  }
}
