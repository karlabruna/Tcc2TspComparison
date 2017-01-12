package runner;

import algorithm.dijkstra.DijkstraCostMatrix;
import algorithm.dijkstra.DijkstraOutOfMemoryError;
import algorithm.dijkstra.DijkstraQueue;
import algorithm.tsp.TspSolver;
import common.Timer;
import data.Inputs;
import dataaccess.GraphDatabaseFactory;
import model.graph.Graph;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Main class for running TSP comparisons between databases.
 */
public class Tcc2TspComparisonRunner {
  private static final Logger logger = Logger.getLogger("TspExperimentLog");

  public static void main(String[] args) {
    // First we get the configuration from the arguments passed to the application.
    GraphDatabaseFactory.DatabaseType dbType = GraphDatabaseFactory.DatabaseType.valueOf(args[0]);
    Graph.AccessMode accessMode = Graph.AccessMode.valueOf(args[1]);
    Graph.Country graphCountry = Graph.Country.valueOf(args[2]);
    DijkstraQueue.Type queueType =
        args.length == 4 ? DijkstraQueue.Type.valueOf(args[3]) : DijkstraQueue.Type.PRIORITY_QUEUE;

    addLoggerFileHandler(dbType, accessMode, graphCountry);

    Timer totalTimer = new Timer();
    totalTimer.startCounter();

    // Retrieves set of 4 nodes given the graph country.
    List<Integer> input = Inputs.getInputForGraphCountry(graphCountry);
    logger.info(String.format("Input: %s", input));
    for (int i = 0; i < 3; i++) {
      Timer graphLoadTimer = new Timer();
      graphLoadTimer.startCounter();
      Graph graph = null;
      try {
        graph = GraphDatabaseFactory.create(dbType, accessMode, graphCountry);
        logger.info(String.format("Loaded graph in: %ss", graphLoadTimer.endCounter()));
        solveTsp(graph, input, queueType);
      } catch (DijkstraOutOfMemoryError e) {
        logger.info("Graph cache size: " + graph.getCacheSize());
        logger.info(graph.getCacheFootprint());
        logger.info(e.getDijkstraNodeMapFootprint());
        logger.info(e.getDijkstraQueueFootprint());
        return;
      } finally {
        // Always close connection.
        if (graph != null) {
          graph.close();
        }
      }
    }
    logger.info(String.format("Total time: %ss", totalTimer.endCounter()));
  }

  private static void solveTsp(Graph graph, List<Integer> nodeIds, DijkstraQueue.Type queueType)
      throws DijkstraOutOfMemoryError {
    Timer timer = new Timer();
    timer.startCounter();
    double[][] costMatrix = DijkstraCostMatrix.buildCostMatrix(graph, nodeIds, true, queueType);
    TspSolver.TspResult tspResult = TspSolver.solveTspProblem(costMatrix, 0);
    logger.info(String.format("Solved in: %ss Length: %s Path: %s Input: %s",
        timer.endCounter(),
        tspResult.getLength(),
        Arrays.toString(tspResult.getSequence()),
        nodeIds));
  }

  private static void addLoggerFileHandler(
      GraphDatabaseFactory.DatabaseType databaseType, Graph.AccessMode accessMode, Graph.Country graphCountry) {
    FileHandler fh;
    try {
      // This block configure the logger with handler and formatter
      fh = new FileHandler(String.format("%s-%s-%s-experiment_results.log", databaseType, accessMode, graphCountry));
      logger.addHandler(fh);
      SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
