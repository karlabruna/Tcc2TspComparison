package model.dijkstra;

import algorithm.dijkstra.FibonacciHeap;
import model.graph.Node;
import model.graph.Relationship;

/**
 * Holds all data for a node to be used when running one to many dijkstra resolution.
 */
public class DijkstraNode {
  private int nodeId;
  private boolean gray;
  private boolean black;
  private double distancetoNode;
//  private FibonacciHeap.Entry<DijkstraNode> queueEntry;

  public DijkstraNode(Node node) {
    this.nodeId = node.getId();
  }

  public int getNodeId() {
    return nodeId;
  }

  public boolean isWhite() {
    return !gray && !black;
  }

  public boolean isGray() {
    return gray;
  }

  public void setGray() {
    this.gray = true;
  }

  public boolean isBlack() {
    return black;
  }

  public void setBlack() {
    this.black = true;
  }

  public double getDistancetoNode() {
    return distancetoNode;
  }

  public void setDistancetoNode(double distancetoNode) {
    this.distancetoNode = distancetoNode;
  }

//  public FibonacciHeap.Entry<DijkstraNode> getQueueEntry() {
//    return queueEntry;
//  }
//
//  public void setQueueEntry(FibonacciHeap.Entry<DijkstraNode> queueEntry) {
//    this.queueEntry = queueEntry;
//  }
}
