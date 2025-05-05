package utils;

public abstract class EnvironmentException {
  public static class EnvironmentUndeclaredException extends Exception {
    public EnvironmentUndeclaredException(String message) {
      super(message);
    }
  }

  public static class EnvironmentAlreadyDeclaredException extends Exception {
    public EnvironmentAlreadyDeclaredException(String message) {
      super(message);
    }
  }
}
