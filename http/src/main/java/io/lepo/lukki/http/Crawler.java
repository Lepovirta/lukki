package io.lepo.lukki.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

public class Crawler {
    private static final Logger log = LoggerFactory.getLogger(Crawler.class);

    private final CloseableHttpAsyncClient httpClient;
    private final ForkJoinPool pool;
    private final Phaser phaser;
    private final ConcurrentMap<String, Boolean> visitedUrls;

    public Crawler() {
        httpClient = HttpAsyncClients.createDefault();
        pool = new ForkJoinPool(2);
        phaser = new Phaser(1);
        visitedUrls = new ConcurrentHashMap<>();
    }

    void run(String url) {
        log.debug("Starting");
        httpClient.start();

        enqueueUrl(url, url);
        phaser.arriveAndAwaitAdvance();
        log.debug("Finished");

        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enqueueUrl(final String rootUrl, final String url) {
        visitedUrls.computeIfAbsent(url, k -> {
            phaser.register();
            pool.execute(() -> {
                getUrl(rootUrl, url);
            });
            return true;
        });
    }

    private void getUrl(String rootUrl, String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            httpClient.execute(httpGet, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    HttpEntity entity = result.getEntity();
                    try (InputStream contentStream = entity.getContent()) {
                        Document document = Jsoup.parse(contentStream, null, url);
                        Elements anchorElements = document.select("a");
                        for (Element anchor : anchorElements) {
                            String href = anchor.absUrl("href");
                            if (href != null && href.startsWith(rootUrl)) {
                                enqueueUrl(rootUrl, href);
                            }
                        }
                        resultToOutput(url, "OK!");
                    } catch (IOException ex) {
                        resultToOutput(url, ex.getMessage());
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                }

                @Override
                public void failed(Exception ex) {
                    try {
                        resultToOutput(url, ex.getMessage());
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                }

                @Override
                public void cancelled() {
                    try {
                        resultToOutput(url, "cancelled!");
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                }
            });
        } catch (IllegalArgumentException ex) {
            resultToOutput(url, ex.getMessage());
            phaser.arriveAndDeregister();
        }
    }

    private void resultToOutput(String url, String message) {
        System.out.println(url + ": " + message);
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.run("https://lepo.io/");
    }
}
