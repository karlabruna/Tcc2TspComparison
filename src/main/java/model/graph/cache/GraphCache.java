package model.graph.cache;

import model.graph.Node;
import model.graph.Relationship;

/**
 * Interface for graph cache.
 */
public interface GraphCache {
  enum Type {
    SLOW,
    FAST
  }

  /* If cache already contains the node for the given id. */
  boolean containsKey(int nodeId);

  /* Retrieves a node from the cache for the given id. */
  Node get(int nodeId);

  /* Inserts a node in the cache. */
  void put(Node node);

  /* Adds a relationship for a node already in the cache. */
  void addRelationship(int nodeId, Relationship relationship);

  /* Returns the size of the cache (number of nodes + number of relationships). */
  int size();

  /* Limits capacity of the cache using its current size. */
  void limitCapacity();
}
