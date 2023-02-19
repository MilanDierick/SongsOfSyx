package settlement.stats;

import snake2d.util.sets.*;
import util.info.INFO;
import util.keymap.RCollection;

public abstract class StatCollection implements INDEXED {

	public final String key;
	public final INFO info;
	private final int index;
	
	static int currentStat;
	
	public abstract LIST<STAT> all();
	
	private RCollection<STAT> coll;
	
	public RCollection<STAT> MAP(){
		return coll;
	}
	
	StatCollection(Init init, String key){
		index = init.holders.add(this);
		init.init(key);
		this.key = key;
		this.info = new INFO(init.jText);
		currentStat = init.stats.size();
	}
	
	@Override
	public int index() {
		return index;
	}
	
	LIST<STAT> makeStats(Init init){
		ArrayList<STAT> all = new ArrayList<>(init.stats.size()-currentStat);
		KeyMap<STAT> map = new KeyMap<>();
		for (int i = currentStat; i < init.stats.size(); i++) {
			if (init.stats.get(i).key() != null) {
				map.put(init.stats.get(i).key(), init.stats.get(i));
				all.add(init.stats.get(i));
			}
		}
		map.expand();
		coll = new RCollection<STAT>(key, map) {

			@Override
			public STAT getAt(int index) {
				return all().get(index);
			}

			@Override
			public LIST<STAT> all() {
				return all;
			}
			
		};
		
		return all;
	}
	
}
