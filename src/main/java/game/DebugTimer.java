package game;

import java.util.*;
import java.util.Map.Entry;

public class DebugTimer {

	private HashMap<Object, Chunk> map = new HashMap<>();
	private long start = System.currentTimeMillis();
	
	public void start(Object o) {
		if (!map.containsKey(o))
			map.put(o, new Chunk(o));
		map.get(o).prev = System.currentTimeMillis();
	}
	
	public void stop(Object o) {
		map.get(o).acc += System.currentTimeMillis()-map.get(o).prev;
	}
	
	public void flush() {
		long n = System.currentTimeMillis();
		if (n - start > 1000) {
			start = n;
			
			flushRegardless();
			
		}
		
	}
	
	public void flushRegardless() {
		Iterator<Entry<Object, Chunk>> sets = map.entrySet().iterator();
		
		while(sets.hasNext()) {
			Chunk c = sets.next().getValue();
			if (c.acc > 0) {
				System.err.println(c.acc + "\t\t" + c.up);
				c.acc = 0;
			}
		}
		
	}
	
	
	private static class Chunk {
		
		private final Object up;
		private long prev = -1;
		private long acc = 0;
		
		Chunk(Object up){
			this.up = up;
		}
		
	}
	
	private static final DebugTimer ss = new DebugTimer();
	
	public static DebugTimer g() {
		return ss;
	}
	
}
