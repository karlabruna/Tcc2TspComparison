package algorithm.dijkstra;

import instrumentation.ObjectSizeFetcher;
import model.dijkstra.DijkstraNode;
import model.graph.Graph;
import model.graph.Node;
import model.graph.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solution for one to many dijkstra algorithm.
 */
public class OneToManyDijkstra {

  public static double[] findShortestPaths(
      Graph graph, int sourceId, List<Integer> targetIds, DijkstraQueue.Type queueType)
      throws DijkstraOutOfMemoryError {

    // Priority queue used.
    DijkstraQueue queue = new DijkstraQueue(queueType);

    // Number of targets remaining to have distance found.
    int remainingTargetsIds = targetIds.size();

    //  Set of all DijkstraNode already visited mapped by id.
    Map<Integer, DijkstraNode> dijkstraNodeMap = new HashMap<>();

    try {
      // Origin is visited and becomes gray.
      Node source = graph.getNode(sourceId);
      DijkstraNode startNode;
      if (!dijkstraNodeMap.containsKey(sourceId)) {
        startNode = new DijkstraNode(source);
        dijkstraNodeMap.put(sourceId, startNode);
      } else {
        startNode = dijkstraNodeMap.get(sourceId);
      }
      startNode.setGray();
      startNode.setDistancetoNode(0.0);
      queue.enqueue(startNode, 0.0);

      while (!queue.isEmpty() && remainingTargetsIds != 0) {
        DijkstraNode dijkstraNode = queue.extractMin();
        List<Relationship> relationships = new ArrayList<>(graph.getNode(dijkstraNode.getNodeId()).getRelationships());
        for (Relationship relationship : relationships) {
          Node target = graph.getNode(relationship.getEndNodeId());
          DijkstraNode toNode;
          if (dijkstraNodeMap.containsKey(target.getId())) {
            toNode = dijkstraNodeMap.get(target.getId());
          } else {
            toNode = new DijkstraNode(target);
            dijkstraNodeMap.put(target.getId(), toNode);
          }
          double newDistance = dijkstraNode.getDistancetoNode() + relationship.getCost();
          if (toNode.isWhite() || newDistance < toNode.getDistancetoNode()) {
            toNode.setDistancetoNode(newDistance);
            if (toNode.isWhite()) {
              // White becomes gray when visited.
              toNode.setGray();
              queue.enqueue(toNode, toNode.getDistancetoNode());
            } else if (toNode.isGray()) {
              // If was already visited, decrease its priority in queue.
              queue.updatePriority(toNode, toNode.getDistancetoNode());
            }
          }
        }
        // After going through all relationships, mark node as black.
        dijkstraNode.setBlack();
        if (targetIds.contains(dijkstraNode.getNodeId())) {
          System.out.println(String.format("Dijkstra remaining targets: %s", --remainingTargetsIds));
        }
      }

      double[] shortestPaths = new double[targetIds.size()];
      for (int i = 0; i < targetIds.size(); i++) {
        if (dijkstraNodeMap.get(targetIds.get(i)) != null) {
          shortestPaths[i] = dijkstraNodeMap.get(targetIds.get(i)).getDistancetoNode();
        } else {
          throw new IllegalArgumentException("Missing routes, not all nodes are connectable.");
        }
      }
      printMemoryUsage(graph, queue, dijkstraNodeMap);
      return shortestPaths;
    } catch (OutOfMemoryError e) {
      printMemoryUsage(graph, queue, dijkstraNodeMap);
      throw new DijkstraOutOfMemoryError(
          ObjectSizeFetcher.getObjectFootprint(queue), ObjectSizeFetcher.getObjectFootprint(dijkstraNodeMap));
    }
  }

  private static void printMemoryUsage(Graph graph, DijkstraQueue queue, Map<Integer, DijkstraNode> dijkstraNodeMap) {
    System.out.println("Graph cache size: " + graph.getCacheSize());
    System.out.println("Queue size: " + queue.size());
    System.out.println("Visited nodes: " + dijkstraNodeMap.size());
  }
}
