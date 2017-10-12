package io.lepo.lukki.testserver;

import io.javalin.Context;
import io.javalin.Javalin;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServer {

  private static final Logger log = LoggerFactory.getLogger(TestServer.class);

  private final int pageCount;
  private final PageGenerator pageGenerator;
  private final Javalin server;
  private Map<String, String> pages = null;

  public TestServer(int port, int pageCount, Long seed) {
    this.pageCount = pageCount;
    pageGenerator = new PageGenerator(seed);
    server = Javalin.create()
        .port(port)
        .get("*", this::handleGet);
  }

  private void handleGet(Context ctx) {
    log.debug("GET: {}", ctx.path());
    String doc = pages.get(ctx.path());
    if (doc == null) {
      ctx.result("Not found");
      ctx.status(404);
    } else {
      ctx.contentType("text/html");
      ctx.result(doc);
      ctx.status(200);
    }
  }

  public void start() {
    if (pages == null) {
      log.info("Generating pages");
      generatePages();
    }
    log.info(
        "Pages generated. Starting HTTP server on port [{}] with seed [{}].",
        server.port(),
        pageGenerator.getSeed()
    );
    server.start();
  }

  private void generatePages() {
    pages = pageGenerator
        .generate(pageCount)
        .entrySet().stream()
        .map(this::formatPage)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private Entry<String, String> formatPage(Entry<String, List<String>> page) {
    String pageLink = page.getKey();
    String html = PageHtmlFormatter.format(pageLink, page.getValue());
    return new SimpleImmutableEntry<>(pageLink, html);
  }

  public void stop() {
    server.stop();
  }

  public int getPageCount() {
    return pageCount;
  }

  public int getPort() {
    return server.port();
  }

  public Long getSeed() {
    return pageGenerator.getSeed();
  }
}
