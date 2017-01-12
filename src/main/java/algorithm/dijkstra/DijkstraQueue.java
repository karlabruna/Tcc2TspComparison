package algorithm.dijkstra;

import model.dijkstra.DijkstraNode;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Queue used to solve Dijkstra single source shortest path problem.
 */
public class DijkstraQueue {

  public enum Type {
    PRIORITY_QUEUE,
//    FIBONACCI_HEAP,
  }

  private Type type;
//  private FibonacciHeap fibonacciHeap;
  private PriorityQueue<DijkstraNode> priorityQueue;

  public DijkstraQueue(Type type) {
    this.type = type;
    switch (type) {
      case PRIORITY_QUEUE:
        //noinspection Since15
        priorityQueue = new PriorityQueue<DijkstraNode>(new DijkstraNodeComparator());
        break;
//      case FIBONACCI_HEAP:
//        fibonacciHeap = new FibonacciHeap<>();
//        break;
      default:
        throw new IllegalArgumentException("Type not supported:" + type);
    }
  }

  public void enqueue(DijkstraNode node, double priority) {
    switch (type) {
      case PRIORITY_QUEUE:
        priorityQueue.add(node);
        break;
//      case FIBONACCI_HEAP:
//        node.setQueueEntry(fibonacciHeap.enqueue(node, priority));
//        break;
      default:
        throw new IllegalArgumentException("Type not supported:" + type);
    }
  }

  public boolean isEmpty() {
    switch (type) {
      case PRIORITY_QUEUE:
        return priorityQueue.isEmpty();
//      case FIBONACCI_HEAP:
//        return fibonacciHeap.isEmpty();
      default:
        throw new IllegalArgumentException("Type not supported:" + type);
    }
  }

  public DijkstraNode extractMin() {
    switch (type) {
      case PRIORITY_QUEUE:
        return (DijkstraNode) priorityQueue.poll();
//      case FIBONACCI_HEAP:
//        return (DijkstraNode) fibonacciHeap.dequeueMin().getValue();
      default:
        throw new IllegalArgumentException("Type not supported:" + type);
    }
  }

  public void updatePriority(DijkstraNode node, double priority) {
    switch (type) {
      case PRIORITY_QUEUE:
        priorityQueue.remove(node);
        priorityQueue.add(node);
        break;
//      case FIBONACCI_HEAP:
//        fibonacciHeap.decreaseKey(node.getQueueEntry(), priority);
//        break;
      default:
        throw new IllegalArgumentException("Type not supported:" + type);
    }
  }

  public int size() {
    switch (type) {
      case PRIORITY_QUEUE:
        return priorityQueue.size();
//      case FIBONACCI_HEAP:
//        fibonacciHeap.decreaseKey(node.getQueueEntry(), priority);
//        break;
      default:
        throw new IllegalArgumentException("Type not supported:" + type);
    }
  }

  public class DijkstraNodeComparator implements Comparator<DijkstraNode> {
    @Override
    public int compare(DijkstraNode x, DijkstraNode y) {
      if (x.getDistancetoNode() > y.getDistancetoNode()) {
        return 1;
      } else if (x.getDistancetoNode() == y.getDistancetoNode()) {
        return 0;
      }
      return -1;
    }
  }
}
