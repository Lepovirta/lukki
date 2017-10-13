package io.lepo.lukki.testserver;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class PageGeneratorTests {

  private final Pattern validPathPattern = Pattern.compile("^/[a-z/]*$");
  private final int[] testCounts = new int[]{11, 102, 324};

  @Test
  @DisplayName("Correct amount of pages are generated")
  void testPageCount() {
    for (int i : testCounts) {
      Assertions
          .assertThat(generatePages(i).size())
          .isEqualTo(i);
    }
  }

  @Test
  @DisplayName("Generated pages contains root page")
  void testContainsRootPage() {
    for (int i : testCounts) {
      Assertions.assertThat(generatePages(i)).containsKey("/");
    }
  }

  @RepeatedTest(5)
  @DisplayName("All paths are in valid format")
  void testLinkFormat() {
    Map<String, List<String>> pages = generatePages(100);
    for (Entry<String, List<String>> page : pages.entrySet()) {
      Assertions.assertThat(page.getKey()).matches(validPathPattern);
      for (String path : page.getValue()) {
        Assertions.assertThat(path).matches(validPathPattern);
      }
    }
  }

  @RepeatedTest(20)
  @DisplayName("All paths are reachable starting from root")
  void testAllPathsReachable() {
    Map<String, List<String>> pages = generatePages(10);
    Set<String> pathsVisited = new HashSet<>(10);
    Queue<String> pathsToProcess = new LinkedList<>();
    pathsToProcess.offer("/"); // Start from root

    while (!pathsToProcess.isEmpty()) {
      // Get the next path and its child links
      String path = pathsToProcess.poll();
      List<String> childLinks = pages.get(path);

      if (childLinks == null) {
        fail("No child links found for path: " + path);
      }

      // Process the child links next
      List<String> nextPaths = new ArrayList<>(childLinks);
      nextPaths.removeAll(pathsVisited);
      pathsToProcess.addAll(nextPaths);

      // Update which paths have been visited
      pathsVisited.add(path);
      pathsVisited.addAll(childLinks);
    }

    Assertions.assertThat(pathsVisited).containsAll(pages.keySet());
  }

  private Map<String, List<String>> generatePages(int validPagesCount) {
    return new PageGenerator().generate(validPagesCount);
  }
}
