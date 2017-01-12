package instrumentation;

import org.openjdk.jol.info.GraphLayout;

/**
 * Used to measure object sizes for memory usage evaluation.
 */
public class ObjectSizeFetcher {
  public static String getObjectFootprint(Object o) {
    return GraphLayout.parseInstance(o).toFootprint();
  }
}
