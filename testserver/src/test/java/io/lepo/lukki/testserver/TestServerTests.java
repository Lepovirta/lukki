package io.lepo.lukki.testserver;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class TestServerTests {

  private final TestServer server = new TestServer(0, 10, null);

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
    HttpResponse response = HttpRequest.get("http://127.0.0.1:" + server.getPort() + "/").send();
    Assertions.assertThat(response.statusCode()).isEqualTo(200);
    Assertions.assertThat(response.contentType()).isEqualTo("text/html");
  }
}
