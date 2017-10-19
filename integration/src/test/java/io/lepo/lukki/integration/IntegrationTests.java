package io.lepo.lukki.integration;

import io.lepo.lukki.core.CrawlEngine;
import io.lepo.lukki.core.CrawlEventAggregator;
import io.lepo.lukki.core.CrawlJob;
import io.lepo.lukki.core.CrawlReport;
import io.lepo.lukki.core.Filters;
import io.lepo.lukki.core.Script;
import io.lepo.lukki.core.ScriptRegistry;
import io.lepo.lukki.html.Html;
import io.lepo.lukki.http.HttpClient;
import io.lepo.lukki.testserver.TestServer;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class IntegrationTests {

  private static final int PAGE_COUNT = 200;

  private final TestServer server = new TestServer(0, PAGE_COUNT, null);

  @BeforeAll
  void beforeAll() {
    server.start();
  }

  @AfterAll
  void afterAll() {
    server.stop();
  }

  @Test
  @DisplayName("Crawler should find all the links")
  void testAllPathsShouldBeFound() throws Exception {
    final Map<String, Script<?>> scripts = new HashMap<>();
    scripts.put(Html.mimeType, Html.script(Collections.emptyList()));
    final ScriptRegistry scriptRegistry = ScriptRegistry.strict(scripts);

    try (
        final CrawlEngine crawler = new CrawlEngine(
            new HttpClient(),
            scriptRegistry,
            Filters.DEFAULT
        )
    ) {
      final CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(testServerUri());
      final CrawlEventAggregator aggregator = crawler.apply(job);
      CrawlReport report = aggregator.getReport().get(60, TimeUnit.SECONDS);

      Assertions.assertThat(report.getFailedCrawls()).isEqualTo(0);
      Assertions.assertThat(report.getSuccessfulCrawls()).isEqualTo(PAGE_COUNT);
      Assertions.assertThat(report.getTotalCrawls()).isEqualTo(PAGE_COUNT);
      Assertions.assertThat(report.getEvents()).hasSize(PAGE_COUNT);
      Assertions.assertThat(report.getEvents()).allMatch(event -> event.getError() == null);
    }
  }

  private URI testServerUri() {
    return URI.create("http://127.0.0.1:" + server.getPort() + "/");
  }
}
