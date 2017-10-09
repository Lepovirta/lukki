package io.lepo.lukki.core;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class Filters {

  public interface LinkFilter extends BiPredicate<CrawlContext, String> {

    LinkFilter skipNone = (context, link) -> true;
    LinkFilter skipAll = (context, link) -> false;
    LinkFilter skipForeignHost = (context, link) -> context.getJob().isSiteUrl(link);
  }

  public interface DocumentFilter extends Predicate<CrawlContext> {

    DocumentFilter skipNone = (context) -> true;
    DocumentFilter skipAll = (context) -> false;
    DocumentFilter skipForeignHost = (context) -> context.getJob().isSiteUrl(context.getUrl());
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
