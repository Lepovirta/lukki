package io.lepo.lukki.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
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

  public URI getUri() {
    return uri;
  }

  public Set<String> getSiteHosts() {
    return siteHosts;
  }

  public Set<String> getSiteHostPrefixes() {
    return siteHostPrefixes;
  }

  public Set<String> getAllSiteHosts() {
    final HashSet<String> set = new HashSet<>(siteHosts);
    set.removeAll(siteHostPrefixes);
    siteHostPrefixes
        .stream().map(s -> "*" + s)
        .forEach(set::add);
    return set;
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

  public Result toResult(
      final LocalDateTime startTime,
      final LocalDateTime endTime
  ) {
    return new Result(this, startTime, endTime);
  }

  public static final class Result {

    private final CrawlJob job;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public Result(
        final CrawlJob job,
        final LocalDateTime startTime,
        final LocalDateTime endTime
    ) {
      this.job = job;
      this.startTime = startTime;
      this.endTime = endTime;
    }

    public Duration getDuration() {
      return Duration.between(startTime, endTime);
    }

    public CrawlJob getJob() {
      return job;
    }

    public LocalDateTime getStartTime() {
      return startTime;
    }

    public LocalDateTime getEndTime() {
      return endTime;
    }
  }
}
