package model.graph.cache;

import model.graph.Node;
import model.graph.Relationship;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache for graph that removes old entries based on custom double linked map.
 */
public class FastGraphCache implements GraphCache {

  private final Map<Integer, Entry> map = new HashMap<>();
  private final Entry head = new Entry(null);

  private int capacity;
  private int elementCount;

  public FastGraphCache(int capacity) {
    this.capacity = capacity;
    head.next = head;
    head.previous = head;
  }

  @Override
  public boolean containsKey(int nodeId) {
    if (map.containsKey(nodeId)) {
      Entry entry = map.get(nodeId);
      remove(entry);
      insert(entry);
      return true;
    }
    return false;
  }

  @Override
  public Node get(int nodeId) {
    return map.get(nodeId).node;
  }

  @Override
  public void put(Node node) {
    Entry entry = new Entry(node);
    map.put(node.getId(), entry);
    insert(entry);
    elementCount += 1 + node.getRelationships().size();
    while (elementCount > capacity) {
      Node oldNode = remove(head.next).node;
      map.remove(oldNode.getId());
      elementCount -= 1 + oldNode.getRelationships().size();
    }
  }

  @Override
  public void addRelationship(int nodeId, Relationship relationship) {
    map.get(nodeId).node.addRelationship(relationship);
    elementCount++;
    while (elementCount > capacity) {
      Node oldNode = remove(head.next).node;
      map.remove(oldNode.getId());
      elementCount -= 1 + oldNode.getRelationships().size();
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Entry entry : map.values()) {
      Node node = entry.node;
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
    System.out.println("Cache capacity limited to: " + capacity);
  }

  private void insert(Entry entry) {
    entry.previous = head.previous;
    entry.next = head;
    head.previous.next = entry;
    head.previous = entry;
  }

  private Entry remove(Entry entry) {
    entry.next.previous = entry.previous;
    entry.previous.next = entry.next;
    return entry;
  }

  private static class Entry {
    Node node;
    Entry previous;
    Entry next;

    Entry(Node node) {
      this.node = node;
      this.previous = null;
      this.next = null;
    }
  }
}
