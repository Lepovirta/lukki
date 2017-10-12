package io.lepo.lukki.testserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PageGenerator {

  private static final String RAND_STRING_CHARS = "abcdefghijklmnopqrstuvwxyz";
  private static final int MAX_NAME_LENGTH = 20;
  private static final int MAX_DIR_DEPTH = 5;
  private final Random random;
  private final Long seed;

  PageGenerator() {
    this(null);
  }

  PageGenerator(Long seed) {
    this.seed = seed;
    this.random = seed == null ? new Random() : new Random(seed);
  }

  Map<String, List<String>> generate(
      int validPageCount
  ) {
    List<String> validPaths = randomPaths(validPageCount - 1);
    validPaths.add(0, "/"); // make sure root is included
    Map<String, List<String>> pages = new HashMap<>(validPageCount);
    List<String> remainingPicks = new ArrayList<>(validPaths);

    // build the pages such that all paths are reachable by starting the crawl from root
    for (String path : validPaths) {
      List<String> childPaths = new ArrayList<>(10);

      if (!remainingPicks.isEmpty()) {
        int picks = randomInt(1, remainingPicks.size());
        List<String> pickedLinks = remainingPicks.subList(0, picks);
        remainingPicks = remainingPicks.subList(picks, remainingPicks.size());
        childPaths.addAll(pickedLinks);
      }

      // add additional paths
      childPaths.addAll(pickRandom(validPaths));
      pages.put(path, childPaths);
    }

    return pages;
  }

  private List<String> randomPaths(int count) {
    return Stream
        .generate(this::randomPath)
        .distinct()
        .limit(count)
        .collect(Collectors.toList());
  }

  private String randomPath() {
    final StringBuilder sb = new StringBuilder("/");
    int dirDepth = randomInt(1, MAX_DIR_DEPTH);
    for (int i = 0; i < dirDepth; i++) {
      randomFileName(sb);
      sb.append('/');
    }
    return sb.toString();
  }

  private void randomFileName(StringBuilder sb) {
    int fileNameLength = randomInt(1, MAX_NAME_LENGTH);
    for (int i = 0; i < fileNameLength; i++) {
      int index = randomInt(RAND_STRING_CHARS.length() - 1);
      sb.append(RAND_STRING_CHARS.charAt(index));
    }
  }

  private <A> List<A> pickRandom(List<A> from) {
    int numberOfElements = randomInt(from.size() + 1);
    return random
        .ints(numberOfElements, 0, from.size())
        .mapToObj(from::get)
        .collect(Collectors.toList());
  }

  private int randomInt(int low, int high) {
    if (high < low) {
      throw new IllegalArgumentException(String.format("%d < %d", high, low));
    }
    return random.nextInt(high - low + 1) + low;
  }

  private int randomInt(int high) {
    return random.nextInt(high);
  }

  public Long getSeed() {
    return seed;
  }
}
