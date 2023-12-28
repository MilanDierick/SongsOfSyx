package init.resources;

import java.util.Arrays;
import java.util.Set;

import init.resources.RBIT.RBITImp;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.keymap.KEY_COLLECTION;

public class ResGroup {

	public final RBIT mask;
	private final ArrayList<ResG> all;
	private final LIST<RESOURCE> resources;
	private int[] indexMap;
	public final String key;
	
	ResGroup(String key, LIST<RESOURCE> resses) {
		this.key = key;
		this.resources = resses;
		all = new ArrayList<>(resses.size());
		indexMap = new int[RESOURCES.ALL().size()];
		Arrays.fill(indexMap, -1);
		RBITImp m = new RBITImp();
		for (RESOURCE r : resources) {
			ResG e = new ResG(all, r);
			m.or(e.resource.bit);
			//em |= e.bit;
			indexMap[r.bIndex()] = e.index();
		}
		
		mask = m;
		
	}
	
	public LIST<RESOURCE> res(){
		return resources;
	}
	
	public boolean is(RESOURCE res) {
		return get(res) != null;
	}
	
	public ResG get(RESOURCE res) {
		if (mask.has(res.bit)) {
			return all.get(indexMap[res.bIndex()]);
		}
		return null;
	}
	
	public LIST<ResG> all(){
		return all;
	}
	
	public RESOURCE[] makeArray() {
		RESOURCE[] rr = new RESOURCE[all.size()];
		for (ResG e : all())
			rr[e.index()] = e.resource;
		return rr;
	}
	
	public final KEY_COLLECTION<ResG> MAP = new KEY_COLLECTION<ResG>() {

		private final String key = "EDIBLE RESOURCE";
		
		@Override
		public ResG tryGet(String value) {
			RESOURCE r = RESOURCES.map().tryGet(value);
			if (r == null)
				return null;
			return ResGroup.this.get(r);
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public LIST<ResG> all() {
			return ResGroup.this.all;
		}
		
		@Override
		public Set<String> available() {
			return RESOURCES.map().available();
		}
		
	};
	
}
