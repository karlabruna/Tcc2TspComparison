package common;

/**
 * Methods to count time for operations.
 */
public class Timer {

  // Global variables for counter;
  private long start;
  private long end;

  public void startCounter() {
    start = System.currentTimeMillis();
  }

  public double endCounter() {
    end = System.currentTimeMillis();
    return (end - start) / 1000.0;
  }
}
