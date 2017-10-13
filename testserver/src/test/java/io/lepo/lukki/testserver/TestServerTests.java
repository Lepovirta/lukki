package io.lepo.lukki.testserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class TestServerTests {

  private static final int PAGE_COUNT = 10;

  private final TestServer server = new TestServer(0, PAGE_COUNT, null);

  @BeforeAll
  void beforeAll() {
    server.start();
  }

  @AfterAll
  void afterAll() {
    server.stop();
  }

  @Test
  @DisplayName("Test server should contain a root document")
  void testRootShouldBeFound() {
    HttpResponse response = getPath("/");
    assertOkResponse(response);
  }

  @Test
  @DisplayName("Test server should have all the paths reachable starting from root")
  void testAllPathsReachable() {
    Set<String> pathsVisited = new HashSet<>(PAGE_COUNT);
    Queue<String> pathsToProcess = new LinkedList<>();
    pathsToProcess.offer("/");

    while(!pathsToProcess.isEmpty()) {
      // Get the next path and its child links
      String path = pathsToProcess.poll();
      HttpResponse response = getPath(path);
      assertOkResponse(response);
      List<String> childLinks = extractLinks(response.bodyText());

      // Process the child links next
      List<String> nextPaths = new ArrayList<>(childLinks);
      nextPaths.removeAll(pathsVisited);
      pathsToProcess.addAll(nextPaths);

      // Update which paths have been visited
      pathsVisited.add(path);
      pathsVisited.addAll(childLinks);
    }

    Assertions.assertThat(pathsVisited).hasSize(PAGE_COUNT);
  }

  private HttpResponse getPath(String path) {
    return HttpRequest.get("http://127.0.0.1:" + server.getPort() + path).send();
  }

  private void assertOkResponse(HttpResponse response) {
    Assertions.assertThat(response.statusCode()).isEqualTo(200);
    Assertions.assertThat(response.contentType()).isEqualTo("text/html");
  }

  private List<String> extractLinks(String htmlString) {
    Document doc = Jsoup.parse(htmlString);
    Elements anchors = doc.select("a");
    return anchors.stream().map(a -> a.attr("href")).collect(Collectors.toList());
  }
}
