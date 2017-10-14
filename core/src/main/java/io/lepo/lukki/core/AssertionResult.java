package io.lepo.lukki.core;

import java.util.Arrays;

public final class AssertionResult {

  private final String name;
  private final String[] errors;

  public AssertionResult(String name, String[] errors) {
    this.name = name;
    this.errors = errors;
  }

  public String getName() {
    return name;
  }

  public String[] getErrors() {
    return errors;
  }

  @Override
  public String toString() {
    return "AssertionResult{"
        + "name='" + name + '\''
        + ", errors=" + Arrays.toString(errors)
        + '}';
  }
}
