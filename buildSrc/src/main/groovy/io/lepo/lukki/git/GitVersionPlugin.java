package io.lepo.lukki.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitVersionPlugin implements Plugin<Project> {

  private static final Logger log = LoggerFactory.getLogger(GitVersionPlugin.class);

  private static final String gitDescribeGlob = "v[0-9]*[0-9]";
  private static final String[] gitDescribeCmd =
      {"git", "describe", "--long", "--match", gitDescribeGlob};
  private static final long waitTime = 1000;
  private static final String alternativeVersion = "noversion";

  private static String runCommand(File directory, String[] command)
      throws IOException, InterruptedException {
    Process proc = Runtime.getRuntime().exec(command, null, directory);

    boolean isFinished = proc.waitFor(waitTime, TimeUnit.MILLISECONDS);
    if (!isFinished) {
      throw new RuntimeException(
          "Command didn't finish in "
              + waitTime + " ms: "
              + String.join(" ", command)
      );
    }

    int code = proc.waitFor();
    if (code != 0) {
      return null;
    }

    BufferedReader stdout = new BufferedReader(
        new InputStreamReader(proc.getInputStream())
    );
    BufferedReader stderr = new BufferedReader(
        new InputStreamReader(proc.getErrorStream())
    );

    forEachLine(stderr, (line) -> log.debug("STDERR: {}", line));

    return readBufferToString(stdout);
  }

  private static String readBufferToString(BufferedReader reader) throws IOException {
    final StringBuilder sb = new StringBuilder(100);
    forEachLine(reader, (line) -> sb.append(line).append("\n"));
    return sb.toString().trim();
  }

  private static void forEachLine(BufferedReader reader, Consumer<String> consumer)
      throws IOException {
    try {
      String line = null;
      while ((line = reader.readLine()) != null) {
        consumer.accept(line);
      }
    } finally {
      reader.close();
    }
  }

  private static String callGitDescribe(File directory) {
    try {
      String describeOutput = runCommand(directory, gitDescribeCmd);
      return describeOutput == null ? alternativeVersion : describeOutput;
    } catch (IOException | InterruptedException ex) {
      log.error("Failed to get version information from Git", ex);
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void apply(Project project) {
    String version = callGitDescribe(project.getRootDir());
    project.getExtensions().getExtraProperties().set("gitVersion", version);
  }
}

