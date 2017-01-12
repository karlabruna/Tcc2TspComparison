package model.tsp;

/**
 * Holds state information for the TSP resolution.
 */
public class Tsp {

  private double maxDistance = 0;
  private double bestLength;
  private int[] currentOrder;
  private int[] bestOrder;

  public Tsp(int n) {
    currentOrder = new int[n];
  }

  public double getMaxDistance() {
    return maxDistance;
  }

  public void setMaxDistance(double maxDistance) {
    this.maxDistance = maxDistance;
  }

  public double getBestLength() {
    return bestLength;
  }

  public void setBestLength(double bestLength) {
    this.bestLength = bestLength;
  }

  public int[] getCurrentOrder() {
    return currentOrder;
  }

  public void setCurrentOrder(int index, int value) {
    currentOrder[index] = value;
  }

  public void setCurrentOrder(int[] currentOrder) {
    this.currentOrder = currentOrder;
  }

  public int[] getBestOrder() {
    return bestOrder;
  }

  public void setBestOrder(int[] bestOrder) {
    this.bestOrder = bestOrder;
  }
}
