package model.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition for a graph node.
 */
public class Node {
  private int id;
  private List<Relationship> relationships;

  /**
   * Only used when loading graph on pre-processing phase (AccessMode.ALL).
   * Node is created with the first relationship found.
   * Following relationships are added using addRelationship.
   */
  public Node(int id, Relationship relationship) {
    this.id = id;
    relationships = new ArrayList<>();
    relationships.add(relationship);
  }

  /**
   * Used when loading graph on demand during Dijkstra solution (AccessMode.ON_DEMAND).
   */
  public Node(int id, List<Relationship> relationships) {
    this.id = id;
    this.relationships = relationships;
  }

  public int getId() {
    return id;
  }

  public List<Relationship> getRelationships() {
    return relationships;
  }

  /**
   * Only used when loading graph on pre-processing phase (AccessMode.ALL).
   * Adds a relationship for an already created Node.
   */
  public void addRelationship(Relationship relationship) {
    relationships.add(relationship);
  }
}
