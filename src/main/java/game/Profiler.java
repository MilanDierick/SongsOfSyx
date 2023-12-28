package game;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import snake2d.LOG;
import snake2d.util.sets.ArrayList;

public interface Profiler {

	public default void logStart(Object o) {
		logStart(o.getClass());
	}
	public void logStart(Class<?> cl);
	public void logEnd(Class<?> cl);
	public default void logEnd(Object o) {
		logEnd(o.getClass());
	}
	public void log();
	
	
	static final Profiler DUMMY = new Profiler() {
		
		@Override
		public void logStart(Class<?> cl) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void logEnd(Class<?> cl) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void log() {
			// TODO Auto-generated method stub
			
		}
	};
	
	public static final Profiler LIVE = new Profiler() {
		
		private final ArrayList<Prof> entries = new ArrayList<Prof>(256);
		{
			while(entries.hasRoom())
				entries.add(new Prof());
		}
		
		private LinkedHashMap<Class<?>, Prof> map = new LinkedHashMap<>();
		private long ss = -1;
		private int tab = 0;
		
		
		@Override
		public void logStart(Class<?> cl) {
			if (ss == -1)
				ss = System.currentTimeMillis();
			
			if (!map.containsKey(cl)) {
				Prof e = entries.removeLast();
				e.acc = 0;
				e.tab = tab;
				e.memAcc = 0;
				map.put(cl, e);
			}
			map.get(cl).mem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			map.get(cl).start = System.nanoTime();
			
			map.get(cl).tab = tab;
			tab++;
		}
		
		@Override
		public void logEnd(Class<?> cl) {
			Prof e = map.get(cl);
			long l = System.nanoTime();
			l -= e.start;
			map.get(cl).memAcc += (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())-map.get(cl).mem;
			e.acc += l;
			tab --;
		}
		
		@Override
		public void log() {
			
			if (ss == -1)
				return;

//			
			if (System.currentTimeMillis() - ss < 2000)
				return;
			
			LOG.ln("CPU");
			for (Entry<Class<?>, Prof> k : map.entrySet()) {
				entries.add(k.getValue());
				
				LOG.ln(LOG.WS(k.getValue().tab*2) + " " + k.getValue().acc/100000 + " " + k.getKey());
			}
			LOG.ln();
			LOG.ln("MEM");
			for (Entry<Class<?>, Prof> k : map.entrySet()) {
				LOG.ln(LOG.WS(k.getValue().tab*2) + " " + k.getValue().memAcc + " " + k.getKey());
			}
			
			map.clear();
			tab = 0;
			ss = -1;
			LOG.ln();
		}
		
		class Prof {
			
			public long start;
			public int tab;
			public long acc;
			public long mem;
			public long memAcc;
		}
		
	};
	
	
}
