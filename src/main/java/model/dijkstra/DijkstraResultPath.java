package model.dijkstra;

import model.graph.Graph;
import model.graph.Node;
import model.graph.Relationship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Path result for dijkstra algorithm.
 */
public class DijkstraResultPath {

//  private double cost;
//  private List<Node> nodes = new ArrayList<>();
//  private List<Relationship> relationships = new ArrayList<>();
//  private List<Object> nodesAndCosts = new ArrayList<>();
//
//  public DijkstraResultPath(Graph graph, DijkstraNode node) {
//    cost = node.getDistancetoNode();
//    nodes.add(graph.getNode(node.getNodeId()));
//    nodesAndCosts.add(node.getNodeId());
//    while(node.hasPredecessor()) {
//      relationships.add(node.getRelationshipToPredecessor());
//      nodes.add(graph.getNode(node.getPredecessorId()));
//      nodesAndCosts.add(node.getRelationshipToPredecessor().getCost());
//      nodesAndCosts.add(node.getPredecessorId());
//      node = node.getPredecessor();
//    }
//    Collections.reverse(nodes);
//    Collections.reverse(relationships);
//    Collections.reverse(nodesAndCosts);
//  }
//
//  public double getCost() {
//    return cost;
//  }
//
//  public void setCost(double cost) {
//    this.cost = cost;
//  }
//
//  public Node startNode() {
//    return nodes.get(0);
//  }
//
//  public Node endNode() {
//    return nodes.get(nodes.size() - 1);
//  }
//
//  public Relationship lastRelationship() {
//    return relationships.get(relationships.size() - 1);
//  }
//
//  public Iterable<Relationship> relationships() {
//    return relationships;
//  }
//
//  public Iterable<Node> nodes() {
//    return nodes;
//  }
//
//  public int length() {
//    return relationships.size();
//  }
//
//  public String toString() {
//    StringBuilder builder = new StringBuilder("cost: ").append(cost).append(" path: ");
//    builder.append(nodesAndCosts.get(0).toString());
//    for (int i = 1; i < nodesAndCosts.size(); i++) {
//      builder.append(" -> ");
//      builder.append(nodesAndCosts.get(i));
//    }
//    return builder.toString();
//  }
}
