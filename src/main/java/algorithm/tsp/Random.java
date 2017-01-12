package algorithm.tsp;

/**
 * Machine independent random number generator based on DIMACS implementation modified for TSP.
 */
public class Random {
  private static final int PRANDMAX = 1000000000;

  private static int[] array = new int[55];
  private static int a;
  private static int b;

  public static void initRand(int seed) {
    int i, ii;
    int last, next;

    seed %= PRANDMAX;
    if (seed < 0) {
      seed += PRANDMAX;
    }
    array[0] = last = seed;
    next = 1;
    for (i = 1; i < 55; i++) {
      ii = (21 * i) % 55;
      array[ii] = next;
      next = last - next;
      if (next < 0) {
        next += PRANDMAX;
      }
      last = array[ii];
    }
    a = 0;
    b = 24;
    for (i = 0; i < 165; i++) {
      rand();
    }
  }

  public static int rand() {
    int t;
    if (a-- == 0) {
      a = 54;
    }
    if (b-- == 0) {
      b = 54;
    }
    t = array[a] - array[b];
    if (t < 0) {
      t += PRANDMAX;
    }
    array[a] = t;
    return t;
  }

  public static double getRandomReal() {
    return (double)rand() / PRANDMAX;
  }

  public static int unifRand(int n) {
    return rand() % n;
  }
}
