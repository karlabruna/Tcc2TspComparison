package model.graph;

/**
 * Represents a graph with its nodes and relationships.
 */
public interface Graph {
  enum Country {
    TINY(2_033, 4_830),
    SMALL(553_195, 1_315_041),
    MEDIUM(3_708_085, 9_046_415),
    LARGE(28_692_913, 72_358_819),
    EU(28_692_913, 72_358_819);

    private int nodes;
    private int relationships;

    Country(int nodes, int relationships) {
      this.nodes = nodes;
      this.relationships = relationships;
    }

    public int getNodes() {
      return nodes;
    }

    public int getRelationships() {
      return relationships;
    }

    public int getSize() {
      return nodes + relationships;
    }
  }

  enum AccessMode {
    ALL,
    ON_DEMAND
  }

  Node getNode(int nodeId);
  void close();

  // Following methods used only for memory usage debugging.
  int getCacheSize();
  String getCacheFootprint();
}
