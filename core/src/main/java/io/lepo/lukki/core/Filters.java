package io.lepo.lukki.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class Filters {

  private static boolean hasSameHost(String url1, String url2) {
    try {
      String host1 = new URL(url1).getHost();
      String host2 = new URL(url2).getHost();
      return host1.equals(host2);
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public interface LinkFilter extends BiPredicate<CrawlContext, String> {

    LinkFilter skipNone = (context, link) -> true;
    LinkFilter skipAll = (context, link) -> false;
    LinkFilter skipForeignHost = (context, link) -> hasSameHost(context.getOriginUrl(), link);
  }

  public interface DocumentFilter extends Predicate<CrawlContext> {

    DocumentFilter skipNone = (context) -> true;
    DocumentFilter skipAll = (context) -> false;
    DocumentFilter skipForeignHost = (context) -> hasSameHost(context.getOriginUrl(), context.getUrl());
  }

  private final LinkFilter linkFilter;
  private final DocumentFilter documentFilter;

  public Filters(
      LinkFilter linkFilter,
      DocumentFilter documentFilter
  ) {
    this.linkFilter = linkFilter;
    this.documentFilter = documentFilter;
  }

  public boolean shouldProcessDocument(CrawlContext context) {
    return documentFilter.test(context);
  }

  public boolean shouldProcessLink(CrawlContext context, String link) {
    return linkFilter.test(context, link);
  }
}
