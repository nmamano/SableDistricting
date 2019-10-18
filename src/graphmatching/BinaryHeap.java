package graphmatching;

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class BinaryHeap<T> {

	Set<T> getValueSet() {
		return valueToHeap.keySet();
	}

    class HeapNode {
        T value;
        double key;
        int pos;
        HeapNode(T value, double key, int pos) {
            this.value = value; this.key = key; this.pos = pos;
        }
		@Override
		public String toString() {
			return "{"+key+": "+value+" ("+pos+")}";
		}
    }
    ArrayList<HeapNode> heap;
    HashMap<T,HeapNode> valueToHeap;
    
	BinaryHeap() {
		heap = new ArrayList<> ();
		valueToHeap = new HashMap<> ();
	}
	
    BinaryHeap(List<T> items, List<Double> keys) {
		heap = new ArrayList<> ();
		valueToHeap = new HashMap<> ();
		for (int i = 0; i < items.size(); i++) {
            HeapNode elem = new HeapNode(items.get(i), keys.get(i), i);
            heap.add(elem);
            valueToHeap.put(items.get(i), elem);
        }	
		heapify();
    }
    
	BinaryHeap(List<T> items, double[] keys) {
		heap = new ArrayList<> (items.size());
		valueToHeap = new HashMap<> ();
		for (int i = 0; i < items.size(); i++) {
            HeapNode elem = new HeapNode(items.get(i), keys[i], i);
            heap.add(elem);
            valueToHeap.put(items.get(i), elem);
        }
		heapify();
	}
	
	private void heapify() {
        for (int i = heap.size()/2; i >= 0; i--) {
            bubbleDown(i);
        }			
	}
	
	@Override
	public String toString() {
		return "Heap: "+heap+" values:" + valueToHeap;
	}

    boolean isEmpty() {
        return heap.isEmpty();
    }
	
	boolean contains(T value) {
		return valueToHeap.containsKey(value);
	}
	
	int size() {
		return heap.size();
	}
    
    T getMin() {
        if (isEmpty()) return null;
        return heap.get(0).value;
    }
    
    double getMinKey() {
		if (isEmpty()) throw new RuntimeException();
        return heap.get(0).key;
    }

    void add(T item, double key) {
		if (valueToHeap.containsKey(item)) {
			throw new RuntimeException("cannot contain repeated items");
		}
        HeapNode elem = new HeapNode(item, key, heap.size());
        valueToHeap.put(item, elem);
        heap.add(elem);
        bubbleUp(heap.size()-1);
    }
    
    void remove(T value) {
        if (!valueToHeap.containsKey(value)) return;
		int pos = valueToHeap.get(value).pos;
        valueToHeap.remove(value);
		if (pos == heap.size()-1) {
			heap.remove(heap.size()-1);
		} else {
			replaceWithLast(pos);
			if (!hasParent(pos)) bubbleDown(pos);
			else {
				double thisKey = heap.get(pos).key;			
				double parentKey = heap.get(parent(pos)).key;
				if (thisKey < parentKey) bubbleUp(pos);
				else bubbleDown(pos);
			}
		}
    }
    
    T extractMin() {
        T value = heap.get(0).value;
        replaceWithLast(0);
        bubbleDown(0);
        valueToHeap.remove(value);
        return value;
    }
	
	ArrayList<T> extractAllSorted() {
		ArrayList<T> res = new ArrayList<> (heap.size());
		while (!isEmpty()) {
			res.add(extractMin());
		}
		return res;
	}
    
    void reduceKey(T value, double newKey) {
		if (!valueToHeap.containsKey(value)) return;
        HeapNode elem = valueToHeap.get(value);
        elem.key = newKey;
        bubbleUp(elem.pos);
    }

	
	
    private boolean hasLeft(int i) {return 2*i+1 < heap.size();}
    private boolean hasRight(int i) {return 2*i+2 < heap.size();}    
    private boolean hasParent(int i) {return i != 0;}
    private int left(int i) {return 2*i+1;}    
    private int right(int i) {return 2*i+2;}
    private int parent(int i) {return (i-1)/2;}
        
    private void bubbleUp(int i) {
        if (!hasParent(i)) return;
        double thisKey = heap.get(i).key;
        double parentKey = heap.get(parent(i)).key;
        if (thisKey < parentKey) {
            swapNodes(i, parent(i));
            bubbleUp(parent(i));
        }
    }
    
    private void bubbleDown(int i) {
        if (!hasLeft(i) && !hasRight(i)) return;
        double thisKey = heap.get(i).key;
        double leftKey = heap.get(left(i)).key;
        if (!hasRight(i)) {
			if (thisKey > leftKey) {
				swapNodes(i, left(i));
				bubbleDown(left(i));
			}
        } else {
            double rightKey = heap.get(right(i)).key;
            int minChild = rightKey < leftKey ? right(i) : left(i);
            double minKey = rightKey < leftKey ? rightKey : leftKey;
            if (thisKey > minKey) {
                swapNodes(i, minChild);
                bubbleDown(minChild);
            }
        }
    }
   
    private void swapNodes(int i, int j) {
        HeapNode aux = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, aux);
        heap.get(i).pos = i;
        heap.get(j).pos = j;
    }

    private void replaceWithLast(int i) {
        heap.set(i, heap.get(heap.size()-1));
        heap.get(i).pos = i;
        heap.remove(heap.size()-1);
    }
	
	static void test() {
		BinaryHeap<String> Q = new BinaryHeap<>();
		Q.testConsistency();
		Q.add("A", 3.55);
		Q.testConsistency();
		Q.add("B", 2.4);
		Q.testConsistency();
		Q.add("C", 1.3);
		Q.testConsistency();
		Q.add("D", 1.8);
		Q.testConsistency();
		Q.add("E", 5);
		Q.testConsistency();
		Q.add("F", 0.5);
		Q.testConsistency();
		Q.add("G", 0.8);
		Q.testConsistency();
		Q.add("H", 8);
		Q.testConsistency();
		Q.add("I", 8);
		Q.testConsistency();
		Q.add("J", 8);
		Q.testConsistency();
		String item = Q.extractMin();
		System.err.println(item);
		Q.testConsistency();
		item = Q.extractMin();
		System.err.println(item);
		Q.testConsistency();
		item = Q.extractMin();
		System.err.println(item);
		Q.testConsistency();
		item = Q.extractMin();
		System.err.println(item);
		Q.testConsistency();
		Q.reduceKey("I", 0.1);
		Q.testConsistency();
		System.err.println(Q);
		Q.remove("I");
		Q.testConsistency();
		System.err.println(Q);
		Q.reduceKey("J", 0.1);
		Q.testConsistency();
		System.err.println(Q);
	}
	
	public boolean testConsistency() {
		if (isEmpty()) {
			if (!heap.isEmpty()) {
				System.err.println("inconsistent empty heap");
				return false;
			}
			if (!valueToHeap.isEmpty()) {
				System.err.println("inconsistent empty heap");
				return false;
			}
			return true;
		}
		//every child bigger than its parent
		if (!testChildOrder(0)) return false;
		if (!testHeapProperty(0)) return false;
		//every value in the map is in the array
		if (valueToHeap.size() != heap.size()) {
			System.err.println("map and heap inconsistent");
			return false;
		}
		if (!testCompleteMap()) return false;
		//every value in the array is in the map
		if (!testCompleteHeap()) return false;
		//every element in the array has its pos correctly
		if (!testPosConsistency()) return false;
		//every element in the map points to the right node
		if (!testMapConsistency()) return false;
		
		return true;
	}
	
	private boolean testHeapProperty(int i) {
		double thisKey = heap.get(i).key;
		if (hasLeft(i)) {
			double leftKey = heap.get(left(i)).key;
			if (thisKey > leftKey) {
				System.err.println("Heap property violated: item "+heap.get(i)+" with bigger key than children "+heap.get(left(i)));
				return false;
			}
			if (!testHeapProperty(left(i))) return false;
		}
		if (hasRight(i)) {
			double rightKey = heap.get(right(i)).key;
			if (thisKey > rightKey) {
				System.err.println("Heap property violated: item "+heap.get(i)+" with bigger key than children "+heap.get(right(i)));
				return false;
			}
			if (!testHeapProperty(right(i))) return false;		
		}
		return true;
	}
	
	private boolean testChildOrder(int i) {
		if (!hasLeft(i) && hasRight(i)) {
			System.err.println("Not complete binary tree");
			return false;
		}
		if (hasLeft(i)) {
			if (!testChildOrder(left(i))) return false;
		}
		if (hasRight(i)) {
			if (!testChildOrder(right(i))) return false;
		}
		return true;
	}
	
	private boolean testCompleteMap() {
		for (T item : valueToHeap.keySet()) {
			HeapNode node = valueToHeap.get(item);
			if (heap.size() <= node.pos) {
				System.err.println("missing item in heap");
				return false;
			} else {
				HeapNode node2 = heap.get(node.pos);
				if (node != node2) {
					System.err.println("map and heap inconsistent");
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean testCompleteHeap() {
		for (HeapNode node : heap) {
			T item = node.value;
			if (!valueToHeap.containsKey(item)) {
				System.err.println("missing value in map");
				return false;
			}
		}
		return true;
	}
	
	private boolean testPosConsistency() {
		int i = 0;
		for (HeapNode node : heap) {
			int pos = node.pos;
			if (pos != i) {
				System.err.println("wrong pos");
				return false;
			}
			i++;
		}
		return true;
	}
	
	private boolean testMapConsistency() {
		for (T item : valueToHeap.keySet()) {
			HeapNode node = valueToHeap.get(item);
			T value = node.value;
			if (item != value) {
				System.err.println("wrong value");
				return false;
			}
		}
		return true;
	}




}