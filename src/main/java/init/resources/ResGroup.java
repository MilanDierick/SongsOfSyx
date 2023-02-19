package init.resources;

import snake2d.util.sets.*;
import util.keymap.RCollection;

public class ResGroup extends RCollection<RESOURCE>{

	public final LIST<RESOURCE> all;
	public final long mask;
	
	ResGroup(String key, RESOURCE... ws) {
		super(key, make(ws));
		if (ws.length == 0)
			throw new RuntimeException();
		all = new ArrayList<>(ws);
		long m = 0;
		for (RESOURCE w : ws) {
			m |= w.bit;
		}
		mask = m;
	}
	
	private static KeyMap<RESOURCE> make(RESOURCE...resources){
		KeyMap<RESOURCE> map = new KeyMap<>();
		for (RESOURCE r : resources)
			map.put(r.key, r);
		return map;
	}
	
	public boolean contains(RESOURCE r) {
		if (r == null)
			return false;
		return (mask & r.bit) > 0;
	}

	@Override
	public RESOURCE getAt(int index) {
		return all.get(index);
	}

	@Override
	public LIST<RESOURCE> all() {
		return all;
	}
	
}
