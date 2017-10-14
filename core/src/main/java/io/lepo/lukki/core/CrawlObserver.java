package io.lepo.lukki.core;

import io.lepo.lukki.core.CrawlJob.Result;
import java.util.function.Consumer;

public interface CrawlObserver {

  void onComplete(CrawlJob.Result result);

  void onNext(CrawlEvent event);

  static CrawlObserver from(Consumer<CrawlJob.Result> onComplete, Consumer<CrawlEvent> onNext) {
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
}
