package io.lepo.lukki.core;

import io.lepo.lukki.core.Filters.DocumentFilter;
import io.lepo.lukki.core.Filters.LinkFilter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
      URI originUri = URI.create(links[0]);
      URI uri = URI.create(links[1]);
      CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(originUri);
      CrawlContext context = createContext(job, originUri);

      Assertions
          .assertThat(LinkFilter.skipForeignHost.test(context, uri))
          .as("no skip: %s - %s", originUri, uri)
          .isTrue();
    }

    for (String[] links : linksShouldSkip) {
      URI originUri = URI.create(links[0]);
      URI uri = URI.create(links[1]);
      CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(originUri);
      CrawlContext context = createContext(job, originUri);

      Assertions
          .assertThat(LinkFilter.skipForeignHost.test(context, uri))
          .as("skip: %s - %s", originUri, uri)
          .isFalse();
    }
  }

  @Test
  @DisplayName("DocumentFilter.skipForeignHost")
  void skipForeignHostDocumentFilter() throws Exception {
    for (String[] links : linksShouldNotSkip) {
      URI originUri = URI.create(links[0]);
      URI uri = URI.create(links[1]);
      CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(originUri);
      CrawlContext context = createContext(job, uri);

      Assertions
          .assertThat(DocumentFilter.skipForeignHost.test(context))
          .as("no skip: %s - %s", links[0], links[1])
          .isTrue();
    }

    for (String[] links : linksShouldSkip) {
      URI originUri = URI.create(links[0]);
      URI uri = URI.create(links[1]);
      CrawlJob job = CrawlJob.withHostsStartingWithUrlHost(originUri);
      CrawlContext context = createContext(job, uri);

      Assertions
          .assertThat(DocumentFilter.skipForeignHost.test(context))
          .as("skip: %s - %s", originUri, uri)
          .isFalse();
    }
  }

  private CrawlContext createContext(CrawlJob job, URI url) {
    return new CrawlContext(
        job, url,
        "text/html", StandardCharsets.UTF_8
    );
  }
}
