package io.lepo.lukki.core;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CrawlReport {

  private final CrawlJob.Result jobResult;
  private final CrawlEvent[] events;
  private final long failedCrawls;

  private CrawlReport(
      final CrawlJob.Result jobResult,
      final CrawlEvent[] events,
      final long failedCrawls
  ) {
    this.jobResult = jobResult;
    this.events = events;
    this.failedCrawls = failedCrawls;
  }

  public static CrawlReport create(
      final CrawlJob.Result jobResult,
      final CrawlEvent[] events
  ) {
    long failures = Arrays.stream(events)
        .filter(CrawlEvent::isFailed)
        .count();

    return new CrawlReport(
        jobResult, events, failures
    );
  }

  public CrawlJob.Result getJobResult() {
    return jobResult;
  }

  public CrawlEvent[] getEvents() {
    return events;
  }

  public long getFailedCrawls() {
    return failedCrawls;
  }

  public int getTotalCrawls() {
    return events.length;
  }

  public long getSuccessfulCrawls() {
    return getTotalCrawls() - getFailedCrawls();
  }

  public String statsString() {
    final StringBuilder sb = new StringBuilder();
    final CrawlJob job = jobResult.getJob();
    final String hosts = job.getAllSiteHosts().stream().collect(Collectors.joining(", "));
    final String summary = String.format(
        "failed [%d], successful [%d], total [%d]",
        getFailedCrawls(), getSuccessfulCrawls(), getTotalCrawls()
    );

    sb.append("Origin:   ").append(job.getUri()).append("\n")
        .append("Hosts:    ").append(hosts).append("\n")
        .append("Duration: ").append(jobResult.getDuration().getSeconds()).append("s \n")
        .append("Summary:  ").append(summary).append("\n")
        .append("Results:\n");

    for (final CrawlEvent event : events) {
      final String prefix = event.isFailed() ? "[ FAIL ] " : "[  OK  ] ";
      indent(sb, 1).append(prefix).append(event.getUri().toString()).append("\n");
      if (event.getError() != null) {
        indent(sb, 2).append("- ").append(event.getError().getMessage()).append("\n");
      }
      for (final AssertionResult assertionResult : event.getAssertionResults()) {
        for (final String error : assertionResult.getErrors()) {
          indent(sb, 2)
              .append("- ").append(assertionResult.getName()).append(": ")
              .append(error).append("\n");
        }
      }
    }

    return sb.toString();
  }

  private StringBuilder indent(final StringBuilder sb, int times) {
    for (int i = 0; i < times; ++i) {
      sb.append("  ");
    }
    return sb;
  }
}
