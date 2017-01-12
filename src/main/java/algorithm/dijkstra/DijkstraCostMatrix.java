package algorithm.dijkstra;

import common.Timer;
import model.graph.Graph;

import java.util.List;

/**
 * Builds cost matrix using Dijkstra single source algorithm for each node.
 */
public class DijkstraCostMatrix {

  public static double[][] buildCostMatrix(
      Graph graph, List<Integer> nodeIds, boolean symmetric, DijkstraQueue.Type queueType)
      throws DijkstraOutOfMemoryError {
    double[][] costMatrix = new double[nodeIds.size()][nodeIds.size()];
    Timer costMatrixTimer = new Timer();
    try {
      // Builds cost matrix using OneToManyDijkstra.
      costMatrixTimer.startCounter();
      for (int i = 0; i < nodeIds.size(); i++) {
        costMatrix[i] = OneToManyDijkstra.findShortestPaths(graph, nodeIds.get(i), nodeIds, queueType);
      }
      System.out.println(String.format("Built distance matrix in: [%f]s", costMatrixTimer.endCounter()));
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      System.out.println(String.format("Failed to build distance matrix in: [%f]s", costMatrixTimer.endCounter()));
    }
    // Make matrix symmetric for TSP.
    if (symmetric) {
      Timer symmetricTimer = new Timer();
      symmetricTimer.startCounter();
      for (int i = 0; i < nodeIds.size(); i++) {
        costMatrix[i][i] = 0.0;
        for (int j = i + 1; j < nodeIds.size(); j++) {
          if (costMatrix[i][j] < 0.0 && costMatrix[j][i] > 0.0) {
            costMatrix[i][j] = costMatrix[j][i];
          } else if (costMatrix[j][i] < 0.0 && costMatrix[i][j] > 0.0) {
            costMatrix[j][i] = costMatrix[i][j];
          } else if (costMatrix[i][j] < 0.0 && costMatrix[j][i] < 0.0) {
            costMatrix[j][i] = -1.0;
            costMatrix[i][j] = -1.0;
          } else {
            double average = (costMatrix[i][j] + costMatrix[j][i]) / 2.0;
            costMatrix[i][j] = average;
            costMatrix[j][i] = average;
          }
        }
      }
      System.out.println(String.format("Made cost matrix symmetric in: [%f]s", symmetricTimer.endCounter()));
    }
    return costMatrix;
  }
}
