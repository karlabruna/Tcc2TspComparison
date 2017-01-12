package algorithm.tsp;

import common.Timer;
import model.tsp.Tsp;

import java.util.Arrays;

/**
 * Used to solve TSP problem.
 */
public class TspSolver {

  // Simulated annealing constants.
  private static final double INITIAL_TEMPERATURE = 100;
  private static final double FINAL_TEMPERATURE = 0.1;
  private static final double COOLING_FACTOR = 0.9;
  private static final int TRIES_PER_TEMPERATURE_PER_N = 500;
  private static final int IMPROVED_PATH_PER_TEMPERATURE_PER_N = 60;

  /**
   * Solves TSP problem for the given parameters.
   *
   * @param costMatrix cost matrix used to solve TSP problem
   * @param startId index for start node
   * @return sequence of node ids for TSP resolution
   */
  public static TspResult solveTspProblem(double[][] costMatrix, int startId) {
    return solveTspProblem(costMatrix, startId, startId);
  }

  /**
   * Solves TSP problem for the given parameters.
   *
   * @param costMatrix cost matrix used to solve TSP problem
   * @param startId index for start node
   * @param endId index for end node
   * @return sequence of node ids for TSP resolution
   */
  public static TspResult solveTspProblem(double[][] costMatrix, int startId, int endId) {
    Timer tspTimer = new Timer();
    tspTimer.startCounter();

    int n = costMatrix.length;
    System.out.println(String.format("In solve_tsp: num: %d, start: %d, end: %d", n, startId, endId));

    if (n < 4) {
      throw new IllegalArgumentException(
          String.format("Error TSP requires four or more locations to optimize. Only %d were supplied.", n));
    }

    // If start and end are the same this is the same as setting end = -1
    if (startId == endId) {
      endId = -1;
    }

    // Fix up matrix id we have an end point.
    // Basically set D(start,end)=INFINITY and D(end,start)=0.0
    if (endId >= 0) {
      System.out.println(String.format("Updating start end costs"));
      costMatrix[startId][endId] = 0.0;
      costMatrix[endId][startId] = 0.0;
    }

    Random.initRand(-314159);

    Tsp tsp = new Tsp(n);

    for (int i = 0; i < costMatrix.length; i++) {
      // Identity permutation.
      tsp.setCurrentOrder(i, i);
      for (int j = 0; j < costMatrix[i].length; j++) {
        if (costMatrix[i][j] > tsp.getMaxDistance()) {
          tsp.setMaxDistance(costMatrix[i][j]);
        }
      }
    }
    tsp.setBestOrder(tsp.getCurrentOrder().clone());
    tsp.setBestLength(getPathLength(costMatrix, n, tsp.getCurrentOrder()));

    // Set up first eulerian path iorder to be improved by
    // simulated annealing.
    findEulerianPath(costMatrix, n, tsp);
    double currentLength = getPathLength(costMatrix, n, tsp.getCurrentOrder());
    if (tsp.getBestLength() < currentLength) {
      tsp.setBestLength(currentLength);
      tsp.setBestOrder(tsp.getBestOrder().clone());
    }

    annealing(costMatrix, n, tsp);

    System.out.println(String.format("Final Path Length: %.4f", getPathLength(costMatrix, n, tsp.getCurrentOrder())));
    tsp.setCurrentOrder(tsp.getBestOrder().clone());
    System.out.println(String.format("Best Path Length: %.4f", tsp.getBestLength()));
    System.out.println(String.format("Best order: %s", Arrays.toString(tsp.getBestOrder())));

    // Reorder ids[] with start as first.

    // Populate array with ids from costMatrix and get index for start and end nodes.
    int[] ids = new int[n];
    int istart = -1, iend = -1;
    for (int i = 0; i < n; i++) {
      ids[i] = i;
      if (ids[i] == startId) {
        istart = i;
      }
      if (ids[i] == endId) {
        iend = i;
      }
    }
    System.out.println(String.format("istart: %d, iend: %d", istart, iend));

    // Get the index of start in current order.
    int jstart = -1, jend = -1;
    for (int i = 0; i < n; i++) {
      if (tsp.getCurrentOrder()[i] == istart) {
        jstart = i;
      }
      if (tsp.getCurrentOrder()[i] == iend) {
        jend = i;
      }
    }
    System.out.println(String.format("jstart: %d, jend: %d", jstart, jend));

    // If the end is specified and the end point and it follow start
    // then we swap start and end and extract the list backwards
    // and later we reverse the list for the desired order.
    boolean reversed = false;
    if ((jend > 0 && jend == jstart + 1) || (jend == 0 && jstart == n - 1)) {
      int tmp = jend;
      jend = jstart;
      jstart = tmp;
      reversed = true;
      System.out.println(String.format("Reversed start and end: jstart: %d, jend: %d", jstart, jend));
    }

    int[] reorderedIds = new int[n];

    // Write reordered ids.
    int j = 0;
    for (int i = jstart; i < n; i++, j++) {
      reorderedIds[j] = ids[tsp.getCurrentOrder()[i]];
    }
    for (int i = 0; i < jstart; i++, j++) {
      reorderedIds[j] = ids[tsp.getCurrentOrder()[i]];
    }

    // If we reversed the order above, now put it correct.
    if (reversed) {
      int tmp = jend;
      jend = jstart;
      jstart = tmp;
      reverse(n, reorderedIds);
    }

    System.out.println(String.format("tsplib: jstart=%d, jend=%d, n=%d, j=%d", jstart, jend, n, j));

    System.out.println(String.format("Solved TSP in: [%f]s", tspTimer.endCounter()));
    return new TspResult(reorderedIds, tsp.getBestLength());
  }

  private static void reverse(int num, int[] ids) {
    int temp;
    for (int i = 0, j = num - 1; i < j; i++, j--) {
      temp = ids[j];
      ids[j] = ids[i];
      ids[i] = temp;
    }
  }

  private static double getPathLength(double[][] costMatrix, int n, int[] path) {
    double length = 0;
    for (int i = 0; i < n - 1; i++) {
      length += costMatrix[path[i]][path[i + 1]];
    }
    // Close path.
    length += costMatrix[path[n - 1]][path[0]];
    return length;
  }

  private static void findEulerianPath(double[][] costMatrix, int n, Tsp tsp) {
    // Keeps parent of each vertex.
    int[] parent = new int[n];
    // Key values (minimum distance) currently set for each vertex.
    double[] keys = new double[n];

    int minimumKeyIndex = -1;
    double minimumKey = tsp.getMaxDistance();
    keys[0] = -1;
    for (int i = 1; i < n; i++) {
      keys[i] = costMatrix[i][0];
      parent[i] = 0;
      if (minimumKey >= keys[i]) {
        minimumKey = keys[i];
        minimumKeyIndex = i;
      }
    }

    // O(n^2) Minimum Spanning Trees by Prim and Jarnick
    // for graphs with adjacency matrix.
    int[] minimumSpanningTree = new int[n];
    for (int i = 0; i < n - 1; i++) {
      // Join minimum distance index with MST.
      // minimumKeyIndex = minimumSpanningTree[i] / n
      // parent[minimumKeyIndex] = minimumSpanningTree[i] % n
      minimumSpanningTree[i] = minimumKeyIndex * n + parent[minimumKeyIndex];
      keys[minimumKeyIndex] = -1;
      minimumKey = tsp.getMaxDistance();
      int newMinimumKeyIndex = minimumKeyIndex;
      for (int j = 0; j < n; j++) {
        // Not connected yet.
        if (keys[j] >= 0) {
          if (keys[j] > costMatrix[j][minimumKeyIndex]) {
            keys[j] = costMatrix[j][minimumKeyIndex];
            parent[j] = minimumKeyIndex;
          }
          if (minimumKey > keys[j]) {
            minimumKey = keys[j];
            newMinimumKeyIndex = j;
          }
        }
      }
      minimumKeyIndex = newMinimumKeyIndex;
    }

    // Preorder Tour of MST
    boolean[] visited = new boolean[n];
    int index = 0;
    ArrayQueue queue = new ArrayQueue(n);
    queue.push(0);
    while (!queue.isEmpty()) {
      int i = queue.pop();
      if (!visited[i]) {
        tsp.setCurrentOrder(index++, i);
        visited[i] = true;
        // Push all kids of i.
        for (int j = 0; j < n - 1; j++) {
          if (i == minimumSpanningTree[j] % n) {
            queue.push(minimumSpanningTree[j] / n);
          }
        }
      }
    }
  }

  private static void annealing(double[][] costMatrix, int n, Tsp tsp) {
    double pathLength = getPathLength(costMatrix, n, tsp.getCurrentOrder());
    // Annealing schedule.
    for (double t = INITIAL_TEMPERATURE; t > FINAL_TEMPERATURE; t *= COOLING_FACTOR) {
      int numOnPath, numNotOnPath;
      int[] path = new int[3];
      int pathChange = 0;
      for (int i = 0; i < TRIES_PER_TEMPERATURE_PER_N * n; i++) {
        do {
          path[0] = Random.unifRand(n);
          path[1] = Random.unifRand(n);

          // Non-empty path.
          if (path[0] == path[1]) {
            path[1] = mod(path[0] + 1, n);
          }
          numOnPath = mod(path[1] - path[0], n) + 1;
          numNotOnPath = n - numOnPath;
        } while(numOnPath < 2 || numNotOnPath < 2); /* non-empty path */

        // Three way.
        double energyChange;
        if (Random.rand() % 2 == 1) {
          do {
            path[2] = mod(Random.unifRand(numNotOnPath) + path[1] + 1, n);
          } while (path[0] == mod(path[2] + 1, n)); /* avoids a non-change */

          energyChange = getThreeWayCost(costMatrix, n, tsp, path);
          if (energyChange < 0 || Random.getRandomReal() < Math.exp(-energyChange / t)) {
            pathChange++;
            pathLength += energyChange;
            doThreeWay(n, tsp, path);
          }
        }
        // Path reverse.
        else {
          energyChange = getReverseCost(costMatrix, n, tsp, path);
          if (energyChange < 0 || Random.getRandomReal() < Math.exp(-energyChange / t)) {
            pathChange++;
            pathLength += energyChange;
            doReverse(n, tsp, path);
          }
        }
        // If the new length is better than best then save it as best.
        if (pathLength < tsp.getBestLength()) {
          tsp.setBestLength(pathLength);
          tsp.setBestOrder(tsp.getCurrentOrder().clone());
        }
        // Finish early.
        if (pathChange > IMPROVED_PATH_PER_TEMPERATURE_PER_N * n) {
          break;
        }
      }
      // If no change then quit.
      if (pathChange == 0) {
        break;
      }
    }
  }

  /*
   * Local Search Heuristics
   *  b-------a        b       a
   *  .       .   =>   .\     /.
   *  . d...e .        . e...d .
   *  ./     \.        .       .
   *  c       f        c-------f
   */
  private static double getThreeWayCost(double[][] costMatrix, int n, Tsp tsp, int[] path) {
    int a = tsp.getCurrentOrder()[mod(path[0] - 1, n)];
    int b = tsp.getCurrentOrder()[path[0]];
    int c = tsp.getCurrentOrder()[path[1]];
    int d = tsp.getCurrentOrder()[mod(path[1] + 1, n)];
    int e = tsp.getCurrentOrder()[path[2]];
    int f = tsp.getCurrentOrder()[mod(path[2] + 1, n)];

    // Add cost between d and e if non symmetric TSP.
    return costMatrix[a][d] + costMatrix[e][b] + costMatrix[c][f]
        - (costMatrix[a][b] + costMatrix[c][d] + costMatrix[e][f]);
  }

  private static void doThreeWay(int n, Tsp tsp, int[] path) {
    int a = mod(path[0] - 1, n);
    int b = path[0];
    int c = path[1];
    int d = mod(path[1] + 1, n);
    int e = path[2];
    int f = mod(path[2] + 1, n);

    int m1 = mod(n + c - b, n) + 1; /* num cities from b to c */
    int m2 = mod(n + a - f, n) + 1; /* num cities from f to a */
    int m3 = mod(n + e - d, n) + 1; /* num cities from d to e */

    int count = 0;
    int[] helper = new int[n];

    // [b..c]
    for (int i = 0; i < m1; i++) {
      helper[count++] = tsp.getCurrentOrder()[mod(i + b, n)];
    }

    // [f..a]
    for (int i = 0; i < m2; i++) {
      helper[count++] = tsp.getCurrentOrder()[mod(i + f, n)];
    }

    // [d..e]
    for (int i = 0; i < m3; i++) {
      helper[count++] = tsp.getCurrentOrder()[mod(i + d, n)];
    }

    // Copy segment back into current order.
    for (int i = 0; i < n; i++) {
      tsp.setCurrentOrder(helper.clone());
    }
  }

  /*
   *   c..b       c..b
   *    \/    =>  |  |
   *    /\        |  |
   *   a  d       a  d
   */
  private static double getReverseCost(double[][] costMatrix, int n, Tsp tsp, int[] path) {
    int a = tsp.getCurrentOrder()[mod(path[0] - 1, n)];
    int b = tsp.getCurrentOrder()[path[0]];
    int c = tsp.getCurrentOrder()[path[1]];
    int d = tsp.getCurrentOrder()[mod(path[1] + 1, n)];

    // Add cost between c and b if non symmetric TSP.
    return costMatrix[d][b] + costMatrix[c][a] - (costMatrix[a][b] + costMatrix[c][d]);
  }

  private static void doReverse(int n, Tsp tsp, int[] path) {
    /* reverse path b...c */
    int nswaps = (mod(path[1] - path[0], n) + 1) / 2;
    for (int i = 0; i < nswaps; i++) {
      int first = mod(path[0] + i, n);
      int last = mod(path[1] - i, n);
      int tmp = tsp.getCurrentOrder()[first];
      tsp.setCurrentOrder(first, tsp.getCurrentOrder()[last]);
      tsp.setCurrentOrder(last, tmp);
    }
  }

  private static int mod(int i, int n) {
    return (i % n >= 0) ? (i % n) : (i % n + n);
  }

  private static class ArrayQueue {
    private int[] queue;
    private int cursor = 0;

    ArrayQueue(int arraySize) {
      queue = new int[arraySize];
    }

    boolean isEmpty() {
      return cursor == 0;
    }

    void push(int value) {
      queue[cursor++] = value;
    }

    int pop() {
      return queue[--cursor];
    }
  }

  public static class TspResult {
    private int[] sequence;
    private Double length;

    public TspResult(int[] sequence, Double length) {
      this.sequence = sequence;
      this.length = length;
    }

    public int[] getSequence() {
      return sequence;
    }

    public Double getLength() {
      return length;
    }
  }
}
