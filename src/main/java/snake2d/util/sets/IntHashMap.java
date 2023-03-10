package snake2d.util.sets;

public final class IntHashMap<T extends IntHashMap.IntHashMapObject> implements ADDABLE<T> {

	private IntHashMapObject table[];
	private int last;

	public IntHashMap() {
		this(20);
	}

	public IntHashMap(int initialCapacity) {
		table = new IntHashMapObject[initialCapacity];
		last = 0;
	}

	@SuppressWarnings("unchecked")
	public T get(int hash) {
		return (T) table[search(hash)];
	}
	
	public void remove(int hash) {
		int i = search(hash);
		if (i < 0)
			throw new RuntimeException();
		last--;
		for (; i < last; i++) {
			table[i] = table[i+1];
		}
	}

	public boolean contains(T object) {
		return search(object.hash()) >= 0;
	}
	
	public boolean contains(int hash) {
		return search(hash) >= 0;
	}

	public int size() {
		return last;
	}

	public boolean isEmpty() {
		return last == 0;
	}

	@Override
	public int add(T e) {
		last++;
		if (last >= table.length) {
			IntHashMapObject table2[] = new IntHashMapObject[table.length*2];
			for (int i = 0; i < table.length; i++) {
				table2[i] = table[i];
			}
			table = table2;
		}
		for (int i = last-1; i >= 0; i--) {
			if (i == 0) {
				table[i] = e;
				return 0;
			}
			if (e.hash() > table[i-1].hash()) {
				table[i] = e;
				return i;
			}
			if (e.hash() == table[i-1].hash())
				throw new RuntimeException();
			table[i] = table[i-1];
				
		}
		return -1;
	}
	
	@Override
	public boolean hasRoom() {
		return true;
	}

	@Override
	public int tryAdd(T e) {
		return add(e);
	}

	private int search(int value) {
		return runBinarySearchIteratively(value, 0, last-1);
	}
	
	private int runBinarySearchIteratively(int key, int low, int high) {

		while (low <= high) {
			int mid = low + ((high - low) / 2);
			if (table[mid].hash() < key) {
				low = mid + 1;
			} else if (table[mid].hash() > key) {
				high = mid - 1;
			} else if (table[mid].hash() == key) {
				return mid;
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<T> toList(){
		ArrayList<T> tt = new ArrayList<>(last);
		for (int i = 0; i < last; i++)
			tt.add((T) table[i]);
		return tt;
	}

	public interface IntHashMapObject {

		public int hash();

	}

}
