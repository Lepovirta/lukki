package io.lepo.lukki.core;

import java.net.URI;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class Filters {

  public interface LinkFilter extends BiPredicate<CrawlContext, URI> {

    LinkFilter skipNone = (context, link) -> true;
    LinkFilter skipAll = (context, link) -> false;
    LinkFilter skipForeignHost = (context, link) -> context.getJob().isSiteUri(link);
  }

  public interface DocumentFilter extends Predicate<CrawlContext> {

    DocumentFilter skipNone = (context) -> true;
    DocumentFilter skipAll = (context) -> false;
    DocumentFilter skipForeignHost = (context) -> context.getJob().isSiteUri(context.getUri());
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

  public boolean shouldProcessLink(CrawlContext context, URI link) {
    return linkFilter.test(context, link);
  }
}
