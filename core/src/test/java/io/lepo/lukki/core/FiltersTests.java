package io.lepo.lukki.core;

import io.lepo.lukki.core.Filters.DocumentFilter;
import io.lepo.lukki.core.Filters.LinkFilter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;

class FiltersTests {

  private static final String[][] linksShouldNotSkip = {
      {"https://lepo.io/", "https://lepo.io/index.html"},
      {"https://lepo.io/", "https://lepo.io/team/index.html"},
      {"https://lepo.io/", "http://lepo.io/"},
      {"https://lepo.io/", "http://lepo.io/helloworld.html"},
      {"https://lepo.io/", "http://www.lepo.io/helloworld.html"}
  };

  private static final String[][] linksShouldSkip = {
      {"https://sub.lepo.io/", "https://lepo.io/"},
      {"https://lepo.io/", "https://google.com/"}
  };


  @Test
  @DisplayName("LinkFilter.skipForeignHost")
  void skipForeignHostLinkFilter() throws Exception {
    for (String[] links : linksShouldNotSkip) {
      CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(links[0]);
      CrawlContext context = createContext(job, links[0]);

      Assertions
          .assertThat(LinkFilter.skipForeignHost.test(context, links[1]))
          .as("no skip: %s - %s", links[0], links[1])
          .isTrue();
    }

    for (String[] links : linksShouldSkip) {
      CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(links[0]);
      CrawlContext context = createContext(job, links[0]);

      Assertions
          .assertThat(LinkFilter.skipForeignHost.test(context, links[1]))
          .as("skip: %s - %s", links[0], links[1])
          .isFalse();
    }
  }

  @Test
  @DisplayName("DocumentFilter.skipForeignHost")
  void skipForeignHostDocumentFilter() throws Exception {
    for (String[] links : linksShouldNotSkip) {
      CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(links[0]);
      CrawlContext context = createContext(job, links[1]);

      Assertions
          .assertThat(DocumentFilter.skipForeignHost.test(context))
          .as("no skip: %s - %s", links[0], links[1])
          .isTrue();
    }

    for (String[] links : linksShouldSkip) {
      CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(links[0]);
      CrawlContext context = createContext(job, links[1]);

      Assertions
          .assertThat(DocumentFilter.skipForeignHost.test(context))
          .as("skip: %s - %s", links[0], links[1])
          .isFalse();
    }
  }

  private CrawlContext createContext(CrawlJob job, String url) {
    return new CrawlContext(
        job, url,
        "text/html", StandardCharsets.UTF_8
    );
  }
}
