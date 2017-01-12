package algorithm.dijkstra;

/**
 * OutOfMemoryError with extra information about size of elements in memory.
 */
public class DijkstraOutOfMemoryError extends Exception {
  private final String dijkstraQueueFootprint;
  private final String dijkstraNodeMapFootprint;

  public DijkstraOutOfMemoryError(String dijkstraQueueFootprint, String dijkstraNodeMapFootprint) {
    this.dijkstraNodeMapFootprint = dijkstraNodeMapFootprint;
    this.dijkstraQueueFootprint = dijkstraQueueFootprint;
  }

  public String getDijkstraQueueFootprint() {
    return dijkstraQueueFootprint;
  }

  public String getDijkstraNodeMapFootprint() {
    return dijkstraNodeMapFootprint;
  }
}
