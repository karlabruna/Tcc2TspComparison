package model.graph.cache;

import model.graph.Node;
import model.graph.Relationship;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache for graph that removes old entries based on custom priority queue.
 */
public class SlowGraphCache implements GraphCache {

  private final PriorityQueue queue = new PriorityQueue();
  private final Map<Integer, Node> nodeMap = new HashMap<>();

  private int capacity;
  private int elementCount;
  private int sequential;

  public SlowGraphCache(int capacity) {
    this.capacity = capacity;
  }

  public boolean containsKey(int nodeId) {
    if (nodeMap.containsKey(nodeId)) {
      queue.update(nodeId, sequential++);
      return true;
    }
    return false;
  }

  public Node get(int nodeId) {
    return nodeMap.get(nodeId);
  }

  public void put(Node node) {
    nodeMap.put(node.getId(), node);
    queue.push(node.getId(), sequential++);
    elementCount += 1 + node.getRelationships().size();

    while (elementCount > capacity) {
      int oldestId = queue.pop();
      elementCount -= 1 + nodeMap.get(oldestId).getRelationships().size();
      nodeMap.remove(oldestId);
    }
  }

  @Override
  public void addRelationship(int nodeId, Relationship relationship) {
    nodeMap.get(nodeId).addRelationship(relationship);
    elementCount++;

    while (elementCount > capacity) {
      int oldestId = queue.pop();
      elementCount -= 1 + nodeMap.get(oldestId).getRelationships().size();
      nodeMap.remove(oldestId);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Node node : nodeMap.values()) {
      for (Relationship relationship : node.getRelationships()) {
        builder
            .append(node.getId())
            .append(",")
            .append(relationship.getCost())
            .append(",")
            .append(relationship.getEndNodeId())
            .append("\n");
      }
    }
    return builder.toString();
  }

  @Override
  public int size() {
    return elementCount;
  }

  @Override
  public void limitCapacity() {
    capacity = elementCount;
  }
}
