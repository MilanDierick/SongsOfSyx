package init.resources;

import java.util.Arrays;
import java.util.Set;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.keymap.KEY_COLLECTION;

public final class Edibles {
	
	public final long mask;
	private final ArrayList<Edible> all;
	private final ArrayList<RESOURCE> resources;
	public final short edibleAllMask;
	private int[] indexMap;
	
	Edibles(LIST<RESOURCE> resses) {
		int size = 0;
		for (RESOURCE r : resses)
			if (r.isEdible())
				size++;
		all = new ArrayList<>(size);
		indexMap = new int[resses.size()];
		Arrays.fill(indexMap, -1);
		long m = 0;
		int em = 0;
		for (RESOURCE r : resses) {
			if (r.isEdible()) {
				Edible e = new Edible(all, r);
				m |= e.resource.bit;
				em |= e.bit;
				indexMap[r.bIndex()] = e.index();
			}
		}
		resources = new ArrayList<>(all.size());
		for (Edible e : all)
			resources.add(e.resource);
		edibleAllMask = (short) em;
		mask = m;
		
	}
	
	public LIST<RESOURCE> res(){
		return resources;
	}
	
	public boolean is(RESOURCE res) {
		return toEdible(res) != null;
	}
	
	public Edible toEdible(RESOURCE res) {
		if (res.isEdible()) {
			return all.get(indexMap[res.bIndex()]);
		}
		return null;
	}
	
	public LIST<Edible> all(){
		return all;
	}
	
	public RESOURCE[] makeArray() {
		RESOURCE[] rr = new RESOURCE[all.size()];
		for (Edible e : all())
			rr[e.index()] = e.resource;
		return rr;
	}
	
	public final KEY_COLLECTION<Edible> MAP = new KEY_COLLECTION<Edible>() {

		private final String key = "EDIBLE RESOURCE";
		
		@Override
		public Edible tryGet(String value) {
			RESOURCE r = RESOURCES.map().tryGet(value);
			if (r == null)
				return null;
			if (!r.isEdible())
				return null;
			return toEdible(r);
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public LIST<Edible> all() {
			return Edibles.this.all;
		}
		
		@Override
		public Set<String> available() {
			return RESOURCES.map().available();
		}
		
	};
	
}
