package model.graph.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Priority queue used by SlowGraphCache.
 */
public class PriorityQueue {
  List<Entry> heap = new ArrayList<>();
  Map<Integer, Integer> index = new HashMap<>();

  PriorityQueue() {
    heap.add(null);
  }

  void push(int id, int value) {
    index.put(id, heap.size());
    heap.add(new Entry(id, value));
    upHeap(heap.size() - 1);
  }

  int pop() {
    int id = heap.get(1).id;
    index.remove(id);
    heap.set(1, heap.get(heap.size() - 1));
    heap.remove(heap.size() - 1);
    index.put(heap.get(1).id, 1);
    downHeap(1);
    return id;
  }

  void update(int id, int value) {
    int pos = index.get(id);
    heap.get(pos).value = value;
    upHeap(pos);
    downHeap(pos);
  }

  private void upHeap(int pos) {
    while(pos > 1) {
      int parent = pos / 2;
      if (heap.get(parent).value > heap.get(pos).value) {
        swap(pos, parent);
        pos = parent;
      } else {
        return;
      }
    }
  }

  private void downHeap(int pos) {
    while (pos <= (heap.size() - 1) / 2) {
      int leftChild = 2 * pos;
      int rightChild = leftChild + 1;
      int smallestChild =
          (rightChild < heap.size() && heap.get(rightChild).value < heap.get(leftChild).value) ? rightChild : leftChild;
      if (heap.get(smallestChild).value < heap.get(pos).value) {
        swap(pos, smallestChild);
        pos = smallestChild;
      } else {
        return;
      }
    }
  }

  private void swap(int left, int right) {
    Entry aux = heap.get(left);
    heap.set(left, heap.get(right));
    heap.set(right, aux);
    index.put(heap.get(left).id, left);
    index.put(heap.get(right).id, right);
  }

  private static class Entry {
    int id;
    int value;

    Entry(int id, int value) {
      this.id = id;
      this.value = value;
    }
  }
}
