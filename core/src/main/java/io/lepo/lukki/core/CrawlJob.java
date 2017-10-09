package io.lepo.lukki.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

public class CrawlJob {

  private final String url;
  private final Set<String> siteHosts;
  private final Set<String> siteHostPrefixes;

  public CrawlJob(
      String url,
      Set<String> siteHosts,
      Set<String> siteHostPrefixes
  ) {
    this.url = url;
    this.siteHosts = Collections.unmodifiableSet(siteHosts);
    this.siteHostPrefixes = Collections.unmodifiableSet(siteHostPrefixes);
  }

  public String getUrl() {
    return url;
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

  public boolean isSiteUrl(String url) {
    try {
      String host = new URL(url).getHost();
      return isSiteHost(host);
    } catch (MalformedURLException ex) {
      return false;
    }
  }

  @Override
  public String toString() {
    return "CrawlJob{"
        + "url='" + url + '\''
        + '}';
  }

  public static CrawlJob withOnlyUrlHost(String url) throws MalformedURLException {
    String host = new URL(url).getHost();
    return new CrawlJob(url, Collections.singleton(host), Collections.emptySet());
  }

  public static CrawlJob withHostsStartingWithUrlHost(String url) throws MalformedURLException {
    String host = new URL(url).getHost();
    return new CrawlJob(url, Collections.singleton(host), Collections.singleton(host));
  }
}
