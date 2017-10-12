package io.lepo.lukki.testserver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
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

  @Test
  @DisplayName("All paths are in valid format")
  @RepeatedTest(2)
  void testLinkFormat() {
    Map<String, List<String>> pages = generatePages(100);
    for (Entry<String, List<String>> page : pages.entrySet()) {
      Assertions.assertThat(page.getKey()).matches(validPathPattern);
      for (String path : page.getValue()) {
        Assertions.assertThat(path).matches(validPathPattern);
      }
    }
  }

  @Test
  @DisplayName("All paths are reachable starting from root")
  @RepeatedTest(5)
  void testAllPathsReachable() {
    Map<String, List<String>> pages = generatePages(10);
    Set<String> pathsToVisit = new HashSet<>(pages.keySet());
    Set<String> pathsVisited = new HashSet<>(10);
    Stack<String> pathsToProcess = new Stack<>();
    pathsToProcess.push("/"); // Start from root

    while (!pathsToProcess.isEmpty()) {
      // Get the next path and its child links
      String path = pathsToProcess.pop();
      List<String> childLinks = pages.getOrDefault(path, Collections.emptyList());

      // Link and its pages have been visited
      pathsToVisit.remove(path);
      pathsToVisit.removeAll(childLinks);

      // Process the child links next
      pathsToProcess.addAll(childLinks);
      pathsToProcess.removeAll(pathsVisited);

      // Ensure the child links are not reprocessed
      pathsVisited.add(path);
      pathsVisited.addAll(childLinks);
    }

    Assertions.assertThat(pathsToVisit).isEmpty();
  }

  private Map<String, List<String>> generatePages(int validPagesCount) {
    return new PageGenerator().generate(validPagesCount);
  }
}
