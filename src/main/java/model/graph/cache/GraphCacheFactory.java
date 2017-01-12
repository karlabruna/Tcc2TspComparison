package model.graph.cache;

/**
 * Factory for creating graph cache implementations.
 */
public class GraphCacheFactory {
  private final static int CAPACITY = 500;

  public static GraphCache create(GraphCache.Type type, boolean useCapacity) {
    int capacity = useCapacity ? CAPACITY : Integer.MAX_VALUE;
    switch (type) {
      case SLOW:
        return new SlowGraphCache(capacity);
      case FAST:
        return new FastGraphCache(capacity);
      default:
        throw new IllegalArgumentException(String.format("Non recognized graph cache type: %s", type));
    }
  }
}
