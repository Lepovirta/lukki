package io.lepo.lukki.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class CrawlJob {

  private final URI uri;
  private final Set<String> siteHosts;
  private final Set<String> siteHostPrefixes;

  public CrawlJob(
      URI uri,
      Set<String> siteHosts,
      Set<String> siteHostPrefixes
  ) {
    this.uri = uri;
    this.siteHosts = Collections.unmodifiableSet(siteHosts);
    this.siteHostPrefixes = Collections.unmodifiableSet(siteHostPrefixes);
  }

  public URI getUri() {
    return uri;
  }

  public Set<String> getSiteHosts() {
    return siteHosts;
  }

  public Set<String> getSiteHostPrefixes() {
    return siteHostPrefixes;
  }

  public boolean isSiteHost(String host) {
    return siteHosts.contains(host) || siteHostPrefixes.stream().anyMatch(host::endsWith);
  }

  public boolean isSiteUri(URI uri) {
    String host = uri.getHost();
    return isSiteHost(host);
  }

  @Override
  public String toString() {
    return "CrawlJob{"
        + "uri='" + uri + '\''
        + '}';
  }

  public static CrawlJob withOnlyUrlHost(URI uri) {
    return new CrawlJob(
        uri,
        Collections.singleton(uri.getHost()),
        Collections.emptySet()
    );
  }

  public static CrawlJob withHostsStartingWithUrlHost(URI uri) throws MalformedURLException {
    Set<String> hosts = Collections.singleton(uri.getHost());
    return new CrawlJob(uri, hosts, hosts);
  }
}
