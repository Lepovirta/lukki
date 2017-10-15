package io.lepo.lukki.core;

import io.lepo.lukki.core.CrawlJob.Result;
import java.util.function.Consumer;

public interface CrawlObserver {

  void onComplete(CrawlJob.Result result);

  void onNext(CrawlEvent event);

  static CrawlObserver from(
      final Consumer<CrawlJob.Result> onComplete,
      final Consumer<CrawlEvent> onNext
  ) {
    return new CrawlObserver() {
      @Override
      public void onComplete(Result result) {
        onComplete.accept(result);
      }

      @Override
      public void onNext(CrawlEvent event) {
        onNext.accept(event);
      }
    };
  }

  default CrawlObserver andThen(final CrawlObserver other) {
    final CrawlObserver current = this;
    return new CrawlObserver() {
      @Override
      public void onComplete(Result result) {
        current.onComplete(result);
        other.onComplete(result);
      }

      @Override
      public void onNext(CrawlEvent event) {
        current.onNext(event);
        other.onNext(event);
      }
    };
  }
}
