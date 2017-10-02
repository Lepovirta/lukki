package io.lepo.lukki.core;

public final class AssertionResult {

  private final String name;
  private final String result;
  private final boolean successful;

  public AssertionResult(String name, String result, boolean successful) {
    this.name = name;
    this.result = result;
    this.successful = successful;
  }

  public String getName() {
    return name;
  }

  public String getResult() {
    return result;
  }

  public boolean isSuccessful() {
    return successful;
  }

  @Override
  public String toString() {
    return "AssertionResult{"
        + "name='" + name + '\''
        + ", result='" + result + '\''
        + ", successful=" + successful
        + '}';
  }
}
